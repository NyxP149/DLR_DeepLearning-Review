package com.dlr.deployment;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RenderBlueprintTest {

    @Test
    @SuppressWarnings("unchecked")
    void definesTheApiAndStaticFrontendWithoutEmbeddingDatabaseSecrets() throws Exception {
        Path blueprint = Path.of("..", "..", "render.yaml").normalize();
        Map<String, Object> document = new Yaml().load(Files.readString(blueprint));
        List<Map<String, Object>> services = (List<Map<String, Object>>) document.get("services");

        assertThat(services).extracting(service -> service.get("name"))
                .containsExactly("dlr-api", "dlr-web");

        Map<String, Object> api = services.getFirst();
        assertThat(api.get("runtime")).isEqualTo("docker");
        assertThat(api.get("healthCheckPath")).isEqualTo("/actuator/health");
        assertThat(api.get("dockerfilePath")).isEqualTo("./apps/api/Dockerfile");

        List<Map<String, Object>> variables = (List<Map<String, Object>>) api.get("envVars");
        assertThat(variables.stream()
                .filter(variable -> List.of("DLR_DB_URL", "DLR_DB_USER", "DLR_DB_PASSWORD").contains(variable.get("key"))))
                .allMatch(variable -> Boolean.FALSE.equals(variable.get("sync")));

        Map<String, Object> web = services.get(1);
        assertThat(web.get("runtime")).isEqualTo("static");
        assertThat(web.get("staticPublishPath")).isEqualTo("./dist/dlr-web/browser");

        List<Map<String, Object>> webVariables = (List<Map<String, Object>>) web.get("envVars");
        assertThat(webVariables).anySatisfy(variable -> {
            assertThat(variable.get("key")).isEqualTo("DLR_HYBRID_API_BASE_URL");
            assertThat(variable.get("sync")).isEqualTo(false);
        });
        assertThat(webVariables).anySatisfy(variable -> {
            assertThat(variable.get("key")).isEqualTo("DLR_EXECUTION_AVAILABLE");
            assertThat(variable.get("value")).isEqualTo("true");
        });
    }
}

