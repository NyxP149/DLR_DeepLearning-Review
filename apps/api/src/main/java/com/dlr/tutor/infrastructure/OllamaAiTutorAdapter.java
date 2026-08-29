package com.dlr.tutor.infrastructure;

import com.dlr.tutor.application.AiTutorPort;
import com.dlr.tutor.application.TutorUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;

@Component
public class OllamaAiTutorAdapter implements AiTutorPort {

    private final RestClient restClient;
    private final String model;

    public OllamaAiTutorAdapter(
            @Value("${dlr.ollama.url:http://localhost:11434}") String url,
            @Value("${dlr.ollama.model:llama3.1:latest}") String model,
            @Value("${dlr.ollama.timeout-seconds:180}") int timeoutSeconds
    ) {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(client);
        factory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));
        this.restClient = RestClient.builder().baseUrl(url).requestFactory(factory).build();
        this.model = model;
    }

    @Override
    public TutorStatus status() {
        try {
            TagsResponse response = restClient.get().uri("/api/tags").retrieve().body(TagsResponse.class);
            List<String> models = response == null || response.models() == null
                    ? List.of()
                    : response.models().stream().map(ModelInfo::name).toList();
            return new TutorStatus(models.contains(model), model, models);
        } catch (RuntimeException exception) {
            return new TutorStatus(false, model, List.of());
        }
    }

    @Override
    public String complete(String systemPrompt, String userPrompt) {
        try {
            ChatResponse response = restClient.post()
                    .uri("/api/chat")
                    .body(new ChatRequest(
                            model,
                            List.of(new Message("system", systemPrompt), new Message("user", userPrompt)),
                            false,
                            false,
                            new Options(0.3, 220)))
                    .retrieve()
                    .body(ChatResponse.class);
            if (response == null || response.message() == null || response.message().content().isBlank()) {
                throw new TutorUnavailableException("Ollama a renvoyé une réponse vide.", null);
            }
            return response.message().content().strip();
        } catch (TutorUnavailableException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new TutorUnavailableException(
                    "Le professeur IA local est indisponible. Le laboratoire reste utilisable sans IA.", exception);
        }
    }

    record ChatRequest(String model, List<Message> messages, boolean stream, boolean think, Options options) {}
    record Message(String role, String content) {}
    record Options(double temperature, int num_predict) {}
    record ChatResponse(Message message) {}
    record TagsResponse(List<ModelInfo> models) {}
    record ModelInfo(String name) {}
}
