package com.dlr.assessment.domain;

import com.dlr.catalog.domain.LabContent;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

/** Note une réponse de quiz ; partagée par le laboratoire et les révisions pour que les deux notent pareil. */
public final class QuizGrader {

    /** Un texte libre est jugé acquis à partir de ce score, partiel à partir de PARTIAL_FROM. */
    public static final int CORRECT_FROM = 75;
    public static final int PARTIAL_FROM = 40;

    private QuizGrader() {
    }

    public static Grade grade(LabContent.QuizQuestion question, Integer selectedChoice, String answerText) {
        if ("SINGLE_CHOICE".equals(question.type())) {
            if (selectedChoice == null || selectedChoice < 0 || selectedChoice >= question.choices().size()) {
                throw new IllegalArgumentException("Un choix valide est obligatoire.");
            }
            boolean correct = selectedChoice.equals(question.correctChoice());
            return new Grade(
                    correct ? BigDecimal.valueOf(100) : BigDecimal.ZERO,
                    correct
                            ? "Bonne réponse."
                            : "Réponse incorrecte. Lis l'explication, puis reformule avec tes mots ce qui te manquait.",
                    List.of());
        }
        if ("FREE_TEXT".equals(question.type())) {
            if (answerText == null || answerText.isBlank()) {
                throw new IllegalArgumentException("Une réponse rédigée est obligatoire.");
            }
            String normalizedAnswer = normalize(answerText);
            List<String> missing = question.expectedKeywords().stream()
                    .filter(keyword -> !normalizedAnswer.contains(normalize(keyword)))
                    .toList();
            int total = question.expectedKeywords().size();
            BigDecimal score = total == 0
                    ? BigDecimal.valueOf(100)
                    : BigDecimal.valueOf(total - missing.size())
                            .multiply(BigDecimal.valueOf(100))
                            .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
            String feedback = missing.isEmpty()
                    ? "Les idées essentielles sont présentes."
                    : "Réponse enregistrée. Idées à préciser : " + String.join(", ", missing) + ".";
            return new Grade(score, feedback, missing);
        }
        throw new IllegalStateException("Type de question non pris en charge : " + question.type());
    }

    /** Résultat d'une réponse de rappel : acquise, partielle ou ratée. */
    public static Outcome outcome(BigDecimal score) {
        if (score.compareTo(BigDecimal.valueOf(CORRECT_FROM)) >= 0) {
            return Outcome.CORRECT;
        }
        return score.compareTo(BigDecimal.valueOf(PARTIAL_FROM)) >= 0 ? Outcome.PARTIAL : Outcome.WRONG;
    }

    private static String normalize(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }

    public enum Outcome { CORRECT, PARTIAL, WRONG }

    public record Grade(BigDecimal score, String feedback, List<String> missingIdeas) {
    }
}
