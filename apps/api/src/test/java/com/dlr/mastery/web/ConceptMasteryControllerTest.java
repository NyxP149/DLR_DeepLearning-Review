package com.dlr.mastery.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ConceptMasteryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void exposesConceptsWithoutLeakingAssessmentSecrets() throws Exception {
        mockMvc.perform(get("/api/mastery/concepts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("JAVA-JVM-BYTECODE"))
                .andExpect(jsonPath("$[0].labCode").value("JAVA-01"))
                .andExpect(jsonPath("$[0].status").isNotEmpty())
                .andExpect(jsonPath("$[0].masteryQuestion").isNotEmpty())
                .andExpect(jsonPath("$[1].status").value("NOT_STARTED"))
                .andExpect(jsonPath("$[1].score").doesNotExist());
    }
}
