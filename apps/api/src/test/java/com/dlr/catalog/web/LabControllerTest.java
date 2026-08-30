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
    void exposesTheCompleteJavaProfessionalPathInOrder() throws Exception {
        mockMvc.perform(get("/api/labs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(32))
                .andExpect(jsonPath("$[0].code").value("JAVA-01"))
                .andExpect(jsonPath("$[5].code").value("JAVA-06"))
                .andExpect(jsonPath("$[22].code").value("JAVA-23"))
                .andExpect(jsonPath("$[22].activityType").value("PROJECT"))
                .andExpect(jsonPath("$[23].code").value("JAVA-24"))
                .andExpect(jsonPath("$[23].activityType").value("CHALLENGE"))
                .andExpect(jsonPath("$[?(@.code == 'PYTHON-01')]").exists())
                .andExpect(jsonPath("$[?(@.code == 'PYTHON-06' && @.activityType == 'PROJECT')]").exists())
                .andExpect(jsonPath("$[?(@.code == 'TYPESCRIPT-01')]").exists())
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
                .andExpect(jsonPath("$.totalLabs").value(6))
                .andExpect(jsonPath("$.labs[1].code").value("PYTHON-02"))
                .andExpect(jsonPath("$.labs[1].prerequisites[0]").value("PYTHON-01"))
                .andExpect(jsonPath("$.labs[5].activityType").value("PROJECT"));
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
}
