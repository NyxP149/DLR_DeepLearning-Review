package com.dlr.catalog.domain;

import java.util.List;

public record PathDescriptor(String code, String title, String status, List<String> professionalObjectives,
                             List<String> prerequisites, List<String> keyConcepts, List<String> activityTypes,
                             String executionEnvironment, String assessmentStrategy, String project,
                             String challenge, List<String> portfolioSkills, int expectedActivityCount) {
    public PathDescriptor {
        professionalObjectives = List.copyOf(professionalObjectives);
        prerequisites = List.copyOf(prerequisites);
        keyConcepts = List.copyOf(keyConcepts);
        activityTypes = List.copyOf(activityTypes);
        portfolioSkills = List.copyOf(portfolioSkills);
    }
}
