package com.dlr.assessment.application;

import com.dlr.catalog.application.LabCatalog;
import com.dlr.catalog.domain.LabContent;
import com.dlr.learning.application.AttemptService;
import com.dlr.learning.domain.Attempt;
import com.dlr.learning.domain.AttemptStatus;
import com.dlr.shared.web.ResourceNotFoundException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Quiz de départ : les premières questions à choix du laboratoire sont posées avant le cours.
 * Aucune correction n'est renvoyée et le score n'est pas touché ; la réponse « Je ne sais pas » est permise.
 * Une fois le laboratoire terminé, le résultat est comparé à la première réponse du quiz final.
 */
@Service
public class PretestService {

    static final int QUESTION_COUNT = 3;
    static final int MIN_QUESTIONS = 2;

    private final LabCatalog catalog;
    private final AttemptService attemptService;
    private final AssessmentRepository assessmentRepository;
    private final JdbcTemplate jdbcTemplate;

    public PretestService(LabCatalog catalog, AttemptService attemptService,
                          AssessmentRepository assessmentRepository, JdbcTemplate jdbcTemplate) {
        this.catalog = catalog;
        this.attemptService = attemptService;
        this.assessmentRepository = assessmentRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public Pretest get(String labCode) {
        LabContent lab = requireLab(labCode);
        List<LabContent.QuizQuestion> questions = selection(lab);
        String status = status(lab, questions);
        return new Pretest(status, questions.stream().map(LabContent.QuizQuestion::code).toList(), gain(lab, questions, status));
    }

    @Transactional
    public Pretest submit(String labCode, List<Answer> answers) {
        LabContent lab = requireLab(labCode);
        List<LabContent.QuizQuestion> questions = selection(lab);
        requirePending(lab, questions);

        Map<String, LabContent.QuizQuestion> byCode = new HashMap<>();
        questions.forEach(question -> byCode.put(question.code(), question));
        Set<String> seen = new HashSet<>();
        if (answers == null || answers.size() != questions.size()) {
            throw new IllegalArgumentException("Réponds aux " + questions.size() + " questions du quiz de départ.");
        }
        for (Answer answer : answers) {
            LabContent.QuizQuestion question = byCode.get(answer.questionId());
            if (question == null || !seen.add(answer.questionId())) {
                throw new IllegalArgumentException("Question inattendue dans le quiz de départ : " + answer.questionId());
            }
            Integer choice = answer.selectedChoice();
            if (choice != null && (choice < 0 || choice >= question.choices().size())) {
                throw new IllegalArgumentException("Choix invalide pour la question " + answer.questionId() + ".");
            }
        }

        Timestamp now = Timestamp.from(Instant.now());
        for (Answer answer : answers) {
            jdbcTemplate.update(
                    "insert into lab_pretest_answer (id, lab_code, question_id, selected_choice, answered_at) values (?, ?, ?, ?, ?)",
                    UUID.randomUUID(), lab.code(), answer.questionId(), answer.selectedChoice(), now);
        }
        jdbcTemplate.update("insert into lab_pretest (lab_code, status, completed_at) values (?, 'ANSWERED', ?)", lab.code(), now);
        return get(lab.code());
    }

    @Transactional
    public Pretest skip(String labCode) {
        LabContent lab = requireLab(labCode);
        requirePending(lab, selection(lab));
        jdbcTemplate.update("insert into lab_pretest (lab_code, status, completed_at) values (?, 'SKIPPED', ?)",
                lab.code(), Timestamp.from(Instant.now()));
        return get(lab.code());
    }

    private void requirePending(LabContent lab, List<LabContent.QuizQuestion> questions) {
        if (!"PENDING".equals(status(lab, questions))) {
            throw new IllegalStateException("Le quiz de départ de " + lab.code() + " n'est plus proposé.");
        }
    }

    private String status(LabContent lab, List<LabContent.QuizQuestion> questions) {
        List<String> stored = jdbcTemplate.queryForList("select status from lab_pretest where lab_code = ?", String.class, lab.code());
        if (!stored.isEmpty()) {
            return stored.get(0);
        }
        if (questions.size() < MIN_QUESTIONS) {
            return "NOT_APPLICABLE";
        }
        // Une tentative déjà entamée (quiz commencé ou laboratoire terminé) ne peut plus être testée « avant le cours ».
        Optional<Attempt> attempt = attemptService.current(lab.code());
        if (attempt.isPresent() && (attempt.get().status() != AttemptStatus.IN_PROGRESS
                || !assessmentRepository.findAnswers(attempt.get().id()).isEmpty())) {
            return "NOT_APPLICABLE";
        }
        return "PENDING";
    }

    /** Les premières questions à choix : leur bonne réponse n'est jamais envoyée au navigateur. */
    private List<LabContent.QuizQuestion> selection(LabContent lab) {
        return lab.quiz().stream()
                .filter(question -> "SINGLE_CHOICE".equals(question.type()))
                .limit(QUESTION_COUNT)
                .toList();
    }

    private Gain gain(LabContent lab, List<LabContent.QuizQuestion> questions, String status) {
        if (!"ANSWERED".equals(status)) {
            return null;
        }
        Optional<Attempt> attempt = attemptService.current(lab.code());
        if (attempt.isEmpty() || attempt.get().status() == AttemptStatus.IN_PROGRESS) {
            return null;
        }
        UUID attemptId = attempt.get().id();
        Map<String, BigDecimal> firstScores = assessmentRepository.firstScores(attemptId);
        Map<String, BigDecimal> finalScores = new HashMap<>();
        assessmentRepository.findAnswers(attemptId).forEach(answer -> finalScores.put(answer.questionId(), answer.score()));

        Map<String, Integer> startChoices = new HashMap<>();
        Set<String> answered = new HashSet<>();
        jdbcTemplate.query("select question_id, selected_choice from lab_pretest_answer where lab_code = ?",
                result -> {
                    answered.add(result.getString("question_id"));
                    int choice = result.getInt("selected_choice");
                    if (!result.wasNull()) startChoices.put(result.getString("question_id"), choice);
                }, lab.code());

        int atStart = 0;
        int atEnd = 0;
        for (LabContent.QuizQuestion question : questions) {
            BigDecimal end = firstScores.getOrDefault(question.code(), finalScores.get(question.code()));
            if (end == null || !answered.contains(question.code())) {
                return null;
            }
            if (question.correctChoice() != null && question.correctChoice().equals(startChoices.get(question.code()))) {
                atStart++;
            }
            if (end.compareTo(BigDecimal.valueOf(100)) >= 0) {
                atEnd++;
            }
        }
        return new Gain(questions.size(), atStart, atEnd);
    }

    private LabContent requireLab(String labCode) {
        return catalog.findByCode(labCode)
                .orElseThrow(() -> new ResourceNotFoundException("LAB_NOT_FOUND", "Laboratoire introuvable : " + labCode));
    }

    public record Answer(String questionId, Integer selectedChoice) {
    }

    /** Résultat du quiz de départ comparé à la première réponse du quiz final, sur les mêmes questions. */
    public record Gain(int total, int correctAtStart, int correctAtEnd) {
    }

    public record Pretest(String status, List<String> questionCodes, Gain gain) {
    }
}
