package com.dlr.adaptation.application;

import com.dlr.catalog.application.LabCatalog;
import org.springframework.stereotype.Component;

/** Énoncé court d'une question de quiz pour les listes du tableau de bord (blocs de code remplacés par « [code] »). */
@Component
class QuestionPrompts {

    private static final int MAX_LENGTH = 160;

    private final LabCatalog catalog;

    QuestionPrompts(LabCatalog catalog) {
        this.catalog = catalog;
    }

    String prompt(String labCode, String questionId) {
        return catalog.findByCode(labCode)
                .flatMap(lab -> lab.quiz().stream().filter(question -> question.code().equals(questionId)).findFirst())
                .map(question -> shorten(question.prompt()))
                .orElse(questionId);
    }

    static String shorten(String text) {
        String flat = text.replaceAll("```[\\s\\S]*?```", "[code]").replaceAll("\\s+", " ").strip();
        return flat.length() <= MAX_LENGTH ? flat : flat.substring(0, MAX_LENGTH - 3) + "...";
    }
}
