package com.dlr.adaptation.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdaptationControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void resetRecommendations() {
        jdbcTemplate.update("delete from adaptation_recommendation");
    }

    @Test
    void explainsAndReplacesARecommendationWithoutSkippingRequiredConcepts() throws Exception {
        String firstBody = mockMvc.perform(get("/api/adaptation/recommendation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reason").isNotEmpty())
                .andExpect(jsonPath("$.targetedConcepts.length()").value(1))
                .andExpect(jsonPath("$.proposedActivity").isNotEmpty())
                .andExpect(jsonPath("$.expectedBenefit").isNotEmpty())
                .andExpect(jsonPath("$.factors.length()").value(3))
                .andExpect(jsonPath("$.status").value("PROPOSED"))
                .andExpect(jsonPath("$.requiresConfirmation").value(true))
                .andReturn().getResponse().getContentAsString();

        JsonNode first = objectMapper.readTree(firstBody);
        String secondBody = mockMvc.perform(post("/api/adaptation/recommendations/{id}/decision", first.get("id").asText())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decision\":\"REPLACE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PROPOSED"))
                .andReturn().getResponse().getContentAsString();

        JsonNode second = objectMapper.readTree(secondBody);
        assertThat(second.get("id").asText()).isNotEqualTo(first.get("id").asText());
        assertThat(second.get("targetedConcepts").get(0).asText())
                .isNotEqualTo(first.get("targetedConcepts").get(0).asText());
    }

    @Test
    void exposesBoundedLocalAutonomyInsightsWithADisclaimer() throws Exception {
        mockMvc.perform(get("/api/adaptation/insights"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autonomyScore").isNumber())
                .andExpect(jsonPath("$.hintDependencyPercent").isNumber())
                .andExpect(jsonPath("$.transferScore").isNumber())
                .andExpect(jsonPath("$.estimatedWeeksRemaining").isNumber())
                .andExpect(jsonPath("$.factors.length()").value(4))
                .andExpect(jsonPath("$.disclaimer").isNotEmpty());
    }
}
