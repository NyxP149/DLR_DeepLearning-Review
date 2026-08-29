package com.dlr.sync.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SyncControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void pairsDevicesKeepsPushesIdempotentAndPreservesConflicts() throws Exception {
        Device first = pair("PC principal");
        Device second = pair("Tablette");
        UUID operation = UUID.randomUUID();
        String firstChange = push(operation, "class Main {}", 1);

        mockMvc.perform(post("/api/sync/changes")
                        .header(HttpHeaders.AUTHORIZATION, bearer(first.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstChange))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accepted").value(1))
                .andExpect(jsonPath("$.duplicates").value(0))
                .andExpect(jsonPath("$.conflicts").value(0))
                .andExpect(jsonPath("$.cursor").value(1));

        mockMvc.perform(post("/api/sync/changes")
                        .header(HttpHeaders.AUTHORIZATION, bearer(first.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstChange))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accepted").value(0))
                .andExpect(jsonPath("$.duplicates").value(1))
                .andExpect(jsonPath("$.cursor").value(1));

        mockMvc.perform(post("/api/sync/changes")
                        .header(HttpHeaders.AUTHORIZATION, bearer(second.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(push(UUID.randomUUID(), "class Main { int variant; }", 1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accepted").value(1))
                .andExpect(jsonPath("$.conflicts").value(1))
                .andExpect(jsonPath("$.cursor").value(2));

        mockMvc.perform(get("/api/sync/changes")
                        .header(HttpHeaders.AUTHORIZATION, bearer(first.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cursor").value(2))
                .andExpect(jsonPath("$.hasMore").value(false))
                .andExpect(jsonPath("$.changes.length()").value(2));

        mockMvc.perform(get("/api/sync/conflicts")
                        .header(HttpHeaders.AUTHORIZATION, bearer(first.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].entityType").value("SUBMISSION"))
                .andExpect(jsonPath("$[0].entityId").value("JAVA-01/Main.java"));

        mockMvc.perform(delete("/api/sync/devices/{id}", second.id())
                        .header(HttpHeaders.AUTHORIZATION, bearer(first.token())))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/sync/changes")
                        .header(HttpHeaders.AUTHORIZATION, bearer(second.token())))
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists("X-Correlation-ID"))
                .andExpect(jsonPath("$.title").value("SYNC_AUTHENTICATION_REQUIRED"));
    }

    @Test
    void rejectsPairingFromAnotherMachineWhenNoPairingCodeIsConfigured() throws Exception {
        mockMvc.perform(post("/api/sync/devices")
                        .with(request -> { request.setRemoteAddr("192.168.1.50"); return request; })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Appareil inconnu\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("SYNC_AUTHENTICATION_REQUIRED"));
    }

    private Device pair(String name) throws Exception {
        String response = mockMvc.perform(post("/api/sync/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(response);
        String token = json.get("token").asText();
        assertThat(token).hasSizeGreaterThan(40);
        return new Device(UUID.fromString(json.get("deviceId").asText()), token);
    }

    private String push(UUID operationId, String code, long version) throws Exception {
        var change = objectMapper.createObjectNode();
        change.put("operationId", operationId.toString());
        change.put("entityType", "SUBMISSION");
        change.put("entityId", "JAVA-01/Main.java");
        change.put("entityVersion", version);
        change.set("payload", objectMapper.createObjectNode().put("code", code));
        change.put("clientModifiedAt", Instant.now().toString());
        var request = objectMapper.createObjectNode();
        request.set("changes", objectMapper.createArrayNode().add(change));
        return objectMapper.writeValueAsString(request);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private record Device(UUID id, String token) {}
}
