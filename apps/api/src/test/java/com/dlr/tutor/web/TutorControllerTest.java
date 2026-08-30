package com.dlr.tutor.web;

import com.dlr.tutor.application.AiTutorPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TutorControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean AiTutorPort tutor;

    @Test
    void reportsStatusAndExplainsAConceptWithoutExposingPrivateTests() throws Exception {
        when(tutor.status()).thenReturn(new AiTutorPort.TutorStatus(true, "test-model", List.of("test-model")));
        when(tutor.complete(anyString(), anyString())).thenReturn("Explication locale structurée.");

        mockMvc.perform(get("/api/tutor/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true));

        mockMvc.perform(post("/api/tutor/explain")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"labCode":"JAVA-01","conceptCode":"JAVA-JVM-BYTECODE","question":"Pourquoi ?"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.model").value("test-model"))
                .andExpect(jsonPath("$.content").value("Explication locale structurée."));
    }

    @Test
    void reviewsAFreeTextAnswerWithoutChangingItsDeterministicScore() throws Exception {
        when(tutor.status()).thenReturn(new AiTutorPort.TutorStatus(true, "test-model", List.of("test-model")));
        when(tutor.complete(anyString(), anyString())).thenReturn("Les idées justes sont présentes. Précise le rôle du bytecode.");

        mockMvc.perform(post("/api/tutor/review-answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"labCode":"JAVA-01","questionCode":"JAVA-01-Q2","answer":"Le bytecode rend le programme portable."}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.purpose").value("FREE_TEXT_REVIEW"))
                .andExpect(jsonPath("$.model").value("test-model"))
                .andExpect(jsonPath("$.content").value("Les idées justes sont présentes. Précise le rôle du bytecode."));

        var prompt = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(tutor).complete(anyString(), prompt.capture());
        assertThat(prompt.getValue())
                .contains("Références pédagogiques fiables", "Le fichier .java contient le code source")
                .doesNotContain("DLR Java Lab 1", "expectedKeywords");
    }

    @Test
    void supportsExactlyTheFivePedagogicalV2Roles() throws Exception {
        when(tutor.status()).thenReturn(new AiTutorPort.TutorStatus(true, "test-model", List.of("test-model")));
        when(tutor.complete(anyString(), anyString())).thenReturn("Retour ciblé du rôle.");

        for (String role : List.of("TEACHER", "COACH", "REVIEWER", "CLIENT", "TECH_LEAD")) {
            mockMvc.perform(post("/api/tutor/consult")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"role":"%s","labCode":"JAVA-01","context":"Mon choix actuel.","question":"Que dois-je améliorer ?"}
                                    """.formatted(role)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.purpose").value("ROLE_" + role))
                    .andExpect(jsonPath("$.content").value("Retour ciblé du rôle."));
        }

        mockMvc.perform(post("/api/tutor/consult")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"role":"RECRUITER","labCode":"JAVA-01","question":"Évalue mon employabilité."}
                                """))
                .andExpect(status().isBadRequest());
        verify(tutor, times(5)).complete(anyString(), anyString());
    }
}
