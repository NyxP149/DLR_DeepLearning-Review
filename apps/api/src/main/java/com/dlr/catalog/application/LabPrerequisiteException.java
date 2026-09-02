package com.dlr.catalog.application;

import java.util.List;

public class LabPrerequisiteException extends RuntimeException {
    private final List<String> missingPrerequisites;

    public LabPrerequisiteException(String labCode, List<String> missingPrerequisites) {
        super("Le laboratoire " + labCode + " nécessite d'abord : " + String.join(", ", missingPrerequisites) + ".");
        this.missingPrerequisites = List.copyOf(missingPrerequisites);
    }

    public List<String> missingPrerequisites() { return missingPrerequisites; }
}
