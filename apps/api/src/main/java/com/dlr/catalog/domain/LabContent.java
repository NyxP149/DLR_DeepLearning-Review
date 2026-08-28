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
        List<String> objectives,
        List<LessonSection> sections,
        List<KeyConcept> keyConcepts,
        List<Exercise> exercises,
        List<QuizQuestion> quiz
) {
    public LabContent {
        objectives = List.copyOf(objectives);
        sections = List.copyOf(sections);
        keyConcepts = List.copyOf(keyConcepts);
        exercises = List.copyOf(exercises);
        quiz = List.copyOf(quiz);
    }

    public record LessonSection(String title, String content) {
    }

    public record KeyConcept(
            String code,
            String name,
            String definition,
            String whyExists,
            String whyImportant,
            String minimalExample,
            String commonMistake,
            String masteryQuestion,
            String masteryProof
    ) {
    }

    public record Exercise(
            String code,
            String title,
            String statement,
            String starterCode
    ) {
    }

    public record QuizQuestion(
            String code,
            String type,
            String prompt,
            List<String> choices,
            Integer correctChoice
    ) {
        public QuizQuestion {
            choices = choices == null ? List.of() : List.copyOf(choices);
        }
    }
}

