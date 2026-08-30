package com.dlr.portfolio.web;

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

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PortfolioControllerTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired JdbcTemplate jdbcTemplate;

    @BeforeEach void clean() {
        jdbcTemplate.update("delete from portfolio_export");
        jdbcTemplate.update("delete from portfolio_decision");
        jdbcTemplate.update("delete from portfolio_project");
    }

    @Test
    void createsPrivateProjectAndExportsAFilteredGithubReadyZip() throws Exception {
        String created = mockMvc.perform(post("/api/portfolio/projects").contentType(MediaType.APPLICATION_JSON).content("""
                {"title":"Fondamentaux Java","summary":"Six laboratoires centrés sur la JVM et la qualité.",
                 "labCodes":["JAVA-01","JAVA-02"],"decisions":["Conserver une évaluation déterministe."]}
                """))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        JsonNode project = objectMapper.readTree(created);
        assertThat(project.get("status").asText()).isEqualTo("PRIVATE");

        String markdown = mockMvc.perform(get("/api/portfolio/projects/{id}/readme", project.get("id").asText()))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(markdown).contains("# Fondamentaux Java", "Compétences démontrées", "Décisions techniques")
                .doesNotContain("score moyen", "displayName", "ai_interaction");

        byte[] archive = mockMvc.perform(get("/api/portfolio/projects/{id}/export", project.get("id").asText()))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();
        Map<String, String> entries = unzip(archive);
        assertThat(entries).containsKeys("README.md", ".gitignore", "docs/PRIVACY.md");
        assertThat(entries.get("README.md")).isEqualTo(markdown);
    }

    @Test
    void blocksPotentialSecretsBeforePersistence() throws Exception {
        mockMvc.perform(post("/api/portfolio/projects").contentType(MediaType.APPLICATION_JSON).content("""
                {"title":"Projet","summary":"api_key=super-secret-value","labCodes":["JAVA-01"],"decisions":[]}
                """))
                .andExpect(status().isBadRequest());
        assertThat(jdbcTemplate.queryForObject("select count(*) from portfolio_project", Integer.class)).isZero();
    }

    private Map<String, String> unzip(byte[] archive) throws Exception {
        Map<String, String> result = new HashMap<>();
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(archive), java.nio.charset.StandardCharsets.UTF_8)) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) result.put(entry.getName(), new String(zip.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8));
        }
        return result;
    }
}
