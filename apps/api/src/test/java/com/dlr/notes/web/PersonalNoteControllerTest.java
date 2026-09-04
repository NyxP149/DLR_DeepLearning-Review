package com.dlr.notes.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PersonalNoteControllerTest {

    @Autowired MockMvc mockMvc;

    @Test
    void savesReadsAndListsANoteWithItsLabMetadata() throws Exception {
        mockMvc.perform(put("/api/labs/JAVA-01/note")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Ma réflexion personnelle sur la JVM.\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.language").value("JAVA"))
                .andExpect(jsonPath("$.content").value("Ma réflexion personnelle sur la JVM."));

        mockMvc.perform(get("/api/labs/JAVA-01/note"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.labTitle").isNotEmpty())
                .andExpect(jsonPath("$.content").value("Ma réflexion personnelle sur la JVM."));

        mockMvc.perform(get("/api/notes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.labCode == 'JAVA-01')].content")
                        .value("Ma réflexion personnelle sur la JVM."));
    }

    @Test
    void persistsAndDeletesAnOllamaReflectionAnalysis() throws Exception {
        mockMvc.perform(put("/api/labs/JAVA-01/reflection-analyses/JAVA-01-Q2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"Bonne intuition. Précise le rôle du bytecode.\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questionId").value("JAVA-01-Q2"));

        mockMvc.perform(get("/api/labs/JAVA-01/reflection-analyses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("Bonne intuition. Précise le rôle du bytecode."));

        mockMvc.perform(delete("/api/labs/JAVA-01/reflection-analyses/JAVA-01-Q2"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/labs/JAVA-01/reflection-analyses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}
