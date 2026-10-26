package com.dlr.catalog.domain;

import java.util.List;

public record LabContent(
        String code,
        String language,
        int number,
        String slug,
        String title,
        String difficulty,
        int threshold,
        String activityType,
        List<String> prerequisites,
        List<String> objectives,
        List<LessonSection> sections,
        List<KeyConcept> keyConcepts,
        List<Exercise> exercises,
        List<QuizQuestion> quiz,
        List<String> checklist
) {
    public LabContent {
        activityType = activityType == null || activityType.isBlank() ? "LAB" : activityType;
        prerequisites = prerequisites == null ? List.of() : List.copyOf(prerequisites);
        objectives = List.copyOf(objectives);
        sections = List.copyOf(sections);
        keyConcepts = List.copyOf(keyConcepts);
        exercises = List.copyOf(exercises);
        quiz = List.copyOf(quiz);
        checklist = List.copyOf(checklist);
    }

    public record LessonSection(String title, String content, List<String> conceptCodes) {
        public LessonSection { conceptCodes = conceptCodes == null ? List.of() : List.copyOf(conceptCodes); }
    }

    public record KeyConcept(
            String code,
            String name,
            String definition,
            String whyExists,
            String whyImportant,
            String minimalExample,
            String professionalExample,
            String codeExample,
            String commonMistake,
            String masteryQuestion,
            String masteryProof
    ) {
    }

    /** `kind` : GUIDED (l'exercice noté du laboratoire, toujours en premier) ou TRANSFER (même notion, contexte nouveau, hors score). */
    public record Exercise(
            String code,
            String title,
            String statement,
            String starterCode,
            String expectedOutput,
            String kind
    ) {
        public static final String GUIDED = "GUIDED";
        public static final String TRANSFER = "TRANSFER";

        public Exercise {
            kind = kind == null || kind.isBlank() ? GUIDED : kind;
        }

        public Exercise(String code, String title, String statement, String starterCode, String expectedOutput) {
            this(code, title, statement, starterCode, expectedOutput, GUIDED);
        }

        public boolean transfer() {
            return TRANSFER.equals(kind);
        }
    }

    /** L'exercice noté du laboratoire : le premier qui n'est pas un transfert. */
    public Exercise guidedExercise() {
        return exercises.stream().filter(exercise -> !exercise.transfer()).findFirst().orElseGet(() -> exercises.getFirst());
    }

    public List<Exercise> transferExercises() {
        return exercises.stream().filter(Exercise::transfer).toList();
    }

    public java.util.Optional<Exercise> exercise(String code) {
        return exercises.stream().filter(exercise -> exercise.code().equals(code)).findFirst();
    }

    public record QuizQuestion(
            String code,
            String type,
            String prompt,
            List<String> choices,
            Integer correctChoice,
            List<String> expectedKeywords,
            String explanation,
            String modelAnswer
    ) {
        public QuizQuestion {
            choices = choices == null ? List.of() : List.copyOf(choices);
            expectedKeywords = expectedKeywords == null ? List.of() : List.copyOf(expectedKeywords);
        }
    }
}
