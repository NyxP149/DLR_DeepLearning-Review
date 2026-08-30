package com.dlr.catalog.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@SpringBootTest
@AutoConfigureMockMvc
class LabControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void exposesJavaLabOne() throws Exception {
        mockMvc.perform(get("/api/labs/JAVA-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("JAVA-01"))
                .andExpect(jsonPath("$.threshold").value(70))
                .andExpect(jsonPath("$.keyConcepts[0].whyExists").isNotEmpty())
                .andExpect(jsonPath("$.exercises[0].starterCode").isNotEmpty())
                .andExpect(jsonPath("$.exercises[0].expectedOutput").doesNotExist())
                .andExpect(jsonPath("$.quiz[0].correctChoice").doesNotExist());
    }

    @Test
    void returnsAStableProblemForAnUnknownLab() throws Exception {
        mockMvc.perform(get("/api/labs/UNKNOWN"))
                .andExpect(status().isNotFound())
                .andExpect(header().exists("X-Correlation-ID"))
                .andExpect(jsonPath("$.title").value("LAB_NOT_FOUND"))
                .andExpect(jsonPath("$.correlationId").isNotEmpty())
                .andExpect(jsonPath("$.suggestedAction").isNotEmpty());
    }

    @Test
    void exposesAllCompletedProfessionalPaths() throws Exception {
        mockMvc.perform(get("/api/labs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(113))
                .andExpect(jsonPath("$[?(@.code == 'JAVA-23' && @.activityType == 'PROJECT')]").exists())
                .andExpect(jsonPath("$[?(@.code == 'JAVA-24' && @.activityType == 'CHALLENGE')]").exists())
                .andExpect(jsonPath("$[?(@.code == 'PYTHON-01')]").exists())
                .andExpect(jsonPath("$[?(@.code == 'PYTHON-24' && @.activityType == 'CHALLENGE')]").exists())
                .andExpect(jsonPath("$[?(@.code == 'TYPESCRIPT-24' && @.activityType == 'CHALLENGE')]").exists())
                .andExpect(jsonPath("$[?(@.code == 'SPRING_BOOT-12' && @.activityType == 'CHALLENGE')]").exists())
                .andExpect(jsonPath("$[?(@.code == 'ANGULAR-10' && @.activityType == 'CHALLENGE')]").exists())
                .andExpect(jsonPath("$[?(@.code == 'SQL-10' && @.activityType == 'CHALLENGE')]").exists())
                .andExpect(jsonPath("$[?(@.code == 'DEVOPS-08' && @.activityType == 'CHALLENGE')]").exists())
                .andExpect(jsonPath("$[?(@.code == 'LLM-01')]").exists());
    }

    @Test
    void exposesDedicatedJavaProgressAndSequentialPrerequisites() throws Exception {
        mockMvc.perform(get("/api/paths/JAVA/progress"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalLabs").value(24))
                .andExpect(jsonPath("$.labs[6].prerequisites[0]").value("JAVA-06"))
                .andExpect(jsonPath("$.labs[22].activityType").value("PROJECT"))
                .andExpect(jsonPath("$.labs[23].activityType").value("CHALLENGE"));
    }

    @Test
    void exposesDedicatedPythonProgressAndSequentialPrerequisites() throws Exception {
        mockMvc.perform(get("/api/paths/PYTHON/progress"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pathCode").value("PYTHON"))
                .andExpect(jsonPath("$.totalLabs").value(24))
                .andExpect(jsonPath("$.labs[1].code").value("PYTHON-02"))
                .andExpect(jsonPath("$.labs[1].prerequisites[0]").value("PYTHON-01"))
                .andExpect(jsonPath("$.labs[22].activityType").value("PROJECT"))
                .andExpect(jsonPath("$.labs[23].activityType").value("CHALLENGE"));
    }

    @Test
    void exposesEveryNewPathWithItsProjectAndChallenge() throws Exception {
        assertPath("TYPESCRIPT", 24);
        assertPath("SPRING_BOOT", 12);
        assertPath("ANGULAR", 10);
        assertPath("SQL", 10);
        assertPath("DEVOPS", 8);
    }

    @Test
    void exposesExtensibleProfessionalPathDescriptors() throws Exception {
        mockMvc.perform(get("/api/paths/catalog"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(8))
                .andExpect(jsonPath("$[0].code").value("JAVA"))
                .andExpect(jsonPath("$[1].code").value("PYTHON"))
                .andExpect(jsonPath("$[2].code").value("TYPESCRIPT"))
                .andExpect(jsonPath("$[3].code").value("LEARN_LLM"))
                .andExpect(jsonPath("$[3].prerequisites[0]").value("PYTHON"))
                .andExpect(jsonPath("$[3].portfolioSkills.length()").isNotEmpty());
    }

    private void assertPath(String code, int total) throws Exception {
        mockMvc.perform(get("/api/paths/{code}/progress", code))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalLabs").value(total))
                .andExpect(jsonPath("$.labs[" + (total - 2) + "].activityType").value("PROJECT"))
                .andExpect(jsonPath("$.labs[" + (total - 1) + "].activityType").value("CHALLENGE"));
    }
}
