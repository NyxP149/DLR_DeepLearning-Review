package com.dlr.tutor.application;

import com.dlr.catalog.application.LabCatalog;
import com.dlr.catalog.domain.LabContent;
import com.dlr.shared.web.ResourceNotFoundException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TutorService {

    private static final String PROMPT_VERSION = "V1";
    private static final String SYSTEM = """
            Tu es le professeur local de DLR. Réponds en français, avec bienveillance et précision.
            Guide l'apprenant par le raisonnement. Ne prétends jamais modifier le score déterministe.
            Pour un indice, ne donne pas la solution complète ni la sortie privée des tests.
            Vise environ 120 mots, sans préambule inutile et sans répéter la question.
            Utilise des phrases courtes et au maximum un fragment minimal de code.
            """;

    private final AiTutorPort tutor;
    private final LabCatalog catalog;
    private final JdbcTemplate jdbcTemplate;

    public TutorService(AiTutorPort tutor, LabCatalog catalog, JdbcTemplate jdbcTemplate) {
        this.tutor = tutor;
        this.catalog = catalog;
        this.jdbcTemplate = jdbcTemplate;
    }

    public AiTutorPort.TutorStatus status() {
        return tutor.status();
    }

    public TutorResponse explain(String labCode, String conceptCode, String question) {
        LabContent lab = lab(labCode);
        LabContent.KeyConcept concept = lab.keyConcepts().stream()
                .filter(item -> item.code().equals(conceptCode))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("CONCEPT_NOT_FOUND", "Concept introuvable : " + conceptCode));
        String prompt = """
                Laboratoire : %s
                Objectifs : %s
                Concept : %s
                Définition : %s
                Pourquoi il existe : %s
                Erreur fréquente : %s
                Question de l'apprenant : %s

                Explique le concept en reliant le pourquoi, le mécanisme et un exemple concret.
                Termine par une question courte pour vérifier la compréhension.
                """.formatted(lab.title(), String.join(" ; ", lab.objectives()), concept.name(), concept.definition(),
                concept.whyExists(), concept.commonMistake(), blankDefault(question, "Donne-moi une explication progressive."));
        return respond("EXPLAIN", prompt);
    }

    public TutorResponse hint(String labCode, String sourceCode, int level) {
        if (level < 1 || level > 3) throw new IllegalArgumentException("Le niveau d'indice doit être compris entre 1 et 3.");
        LabContent lab = lab(labCode);
        String safeCode = sourceCode == null ? "Aucun code pour le moment." : sourceCode.substring(0, Math.min(sourceCode.length(), 12_000));
        String instruction = switch (level) {
            case 1 -> "Pose une question orientante sans proposer de code.";
            case 2 -> "Indique la structure ou l'API Java à envisager, sans solution complète.";
            default -> "Montre un fragment minimal différent de la solution finale et explique comment l'adapter.";
        };
        String prompt = """
                Laboratoire : %s
                Consigne visible : %s
                Code actuel :
                %s

                Niveau d'indice : %d. %s
                """.formatted(lab.title(), lab.exercises().getFirst().statement(), safeCode, level, instruction);
        return respond("HINT_" + level, prompt);
    }

    public TutorResponse reviewAnswer(String labCode, String questionCode, String answer) {
        LabContent lab = lab(labCode);
        LabContent.QuizQuestion question = lab.quiz().stream()
                .filter(item -> item.code().equals(questionCode) && "FREE_TEXT".equals(item.type()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "QUIZ_QUESTION_NOT_FOUND", "Question libre introuvable : " + questionCode));
        String safeAnswer = answer.strip().substring(0, Math.min(answer.strip().length(), 4_000));
        String trustedReferences = lab.sections().stream()
                .map(section -> section.title() + " : " + section.content())
                .collect(Collectors.joining("\n"));
        String conceptReferences = lab.keyConcepts().stream()
                .map(concept -> concept.name() + " : " + concept.definition() + " " + concept.whyExists())
                .collect(Collectors.joining("\n"));
        String prompt = """
                Laboratoire : %s
                Objectifs : %s
                Question ouverte : %s
                Références pédagogiques fiables :
                %s
                %s

                Réponse de l'apprenant :
                %s

                Fais une correction exclusivement qualitative, sans note et sans modifier le score.
                Utilise uniquement les références pédagogiques ci-dessus pour établir les faits ; n'invente aucun prérequis ni aucune restriction.
                Commence par « Critères utilisés : » et cite brièvement les idées publiques attendues, sans révéler de barème caché.
                Identifie d'abord les idées justes, puis une imprécision ou un manque important.
                Si la réponse est déjà exacte au regard des références, dis-le clairement sans fabriquer d'erreur.
                Propose enfin une reformulation courte et termine par une question de réflexion.
                """.formatted(lab.title(), String.join(" ; ", lab.objectives()), question.prompt(),
                trustedReferences, conceptReferences, safeAnswer);
        return respond("FREE_TEXT_REVIEW", prompt);
    }

    private TutorResponse respond(String purpose, String prompt) {
        String response = tutor.complete(SYSTEM, prompt);
        String model = tutor.status().selectedModel();
        jdbcTemplate.update(
                """
                insert into ai_interaction (id, purpose, model, prompt_version, input_hash, response, created_at)
                values (?, ?, ?, ?, ?, ?, ?)
                """,
                UUID.randomUUID(), purpose, model, PROMPT_VERSION, hash(prompt), response, Timestamp.from(Instant.now()));
        return new TutorResponse(purpose, model, response);
    }

    private LabContent lab(String code) {
        return catalog.findByCode(code).orElseThrow(() ->
                new ResourceNotFoundException("LAB_NOT_FOUND", "Laboratoire introuvable : " + code));
    }

    private String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 indisponible", exception);
        }
    }

    private String blankDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.strip();
    }

    public record TutorResponse(String purpose, String model, String content) {}
}
