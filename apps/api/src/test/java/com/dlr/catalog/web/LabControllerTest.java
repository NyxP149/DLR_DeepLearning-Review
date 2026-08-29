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
    void exposesTheSixJavaMvpLabsInOrder() throws Exception {
        mockMvc.perform(get("/api/labs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(6))
                .andExpect(jsonPath("$[0].code").value("JAVA-01"))
                .andExpect(jsonPath("$[5].code").value("JAVA-06"));
    }
}
