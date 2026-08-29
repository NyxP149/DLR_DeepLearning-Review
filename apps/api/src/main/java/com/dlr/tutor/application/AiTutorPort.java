package com.dlr.tutor.application;

import java.util.List;

public interface AiTutorPort {

    TutorStatus status();

    String complete(String systemPrompt, String userPrompt);

    record TutorStatus(boolean available, String selectedModel, List<String> installedModels) {
    }
}
