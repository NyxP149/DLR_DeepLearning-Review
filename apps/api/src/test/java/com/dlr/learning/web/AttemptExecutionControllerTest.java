package com.dlr.learning.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AttemptExecutionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void startsAndReloadsAnAttempt() throws Exception {
        String body = mockMvc.perform(post("/api/labs/JAVA-01/attempts"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.labCode").value("JAVA-01"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID attemptId = UUID.fromString(objectMapper.readTree(body).get("id").asText());

        mockMvc.perform(get("/api/attempts/{id}", attemptId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(attemptId.toString()));
    }

    @Test
    void savesASubmissionWithoutReturningItsSourceCode() throws Exception {
        String attemptBody = mockMvc.perform(post("/api/labs/JAVA-01/attempts"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String attemptId = objectMapper.readTree(attemptBody).get("id").asText();

        JsonNode request = objectMapper.createObjectNode()
                .put("language", "JAVA")
                .put("sourceCode", "public class Main { public static void main(String[] args) {} }")
                .put("origin", "EDITOR");

        mockMvc.perform(post("/api/attempts/{id}/submissions", attemptId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.attemptId").value(attemptId))
                .andExpect(jsonPath("$.language").value("JAVA"))
                .andExpect(jsonPath("$.sourceCode").doesNotExist());
    }

    @Test
    void refusesASubmissionForAnUnknownAttempt() throws Exception {
        JsonNode request = objectMapper.createObjectNode()
                .put("language", "JAVA")
                .put("sourceCode", "public class Main {}")
                .put("origin", "EDITOR");

        mockMvc.perform(post("/api/attempts/{id}/submissions", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("ATTEMPT_NOT_FOUND"));
    }
}

