package com.dlr.execution.web;

import com.dlr.execution.application.RunnerAvailability;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ExecutionStatusControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean RunnerAvailability runnerAvailability;

    @Test
    void exposesTheRealRunnerAvailability() throws Exception {
        when(runnerAvailability.status()).thenReturn(new RunnerAvailability.Status(
                true, "LOCAL_DOCKER", "Runner Docker local connecté et prêt."));

        mockMvc.perform(get("/api/execution/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.mode").value("LOCAL_DOCKER"))
                .andExpect(jsonPath("$.message").value("Runner Docker local connecté et prêt."));
    }
}
