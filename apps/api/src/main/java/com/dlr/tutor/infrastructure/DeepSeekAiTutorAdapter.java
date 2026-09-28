package com.dlr.tutor.infrastructure;

import com.dlr.tutor.application.AiTutorPort;
import com.dlr.tutor.application.TutorUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;

@Component
@ConditionalOnProperty(prefix = "dlr.tutor", name = "provider", havingValue = "deepseek")
public class DeepSeekAiTutorAdapter implements AiTutorPort {

    private final RestClient restClient;
    private final String model;
    private final String apiKey;
    private final int maxTokens;

    public DeepSeekAiTutorAdapter(
            @Value("${dlr.deepseek.url:https://api.deepseek.com}") String url,
            @Value("${dlr.deepseek.api-key:}") String apiKey,
            @Value("${dlr.deepseek.model:deepseek-chat}") String model,
            @Value("${dlr.deepseek.max-tokens:700}") int maxTokens,
            @Value("${dlr.deepseek.timeout-seconds:60}") int timeoutSeconds
    ) {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(client);
        factory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));
        this.restClient = RestClient.builder().baseUrl(url).requestFactory(factory).build();
        this.model = model;
        this.apiKey = apiKey;
        this.maxTokens = maxTokens;
    }

    @Override
    public TutorStatus status() {
        boolean configured = apiKey != null && !apiKey.isBlank();
        return new TutorStatus(configured, model, configured ? List.of(model) : List.of());
    }

    @Override
    public String complete(String systemPrompt, String userPrompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new TutorUnavailableException(
                    "Clé API DeepSeek manquante. Configure DLR_DEEPSEEK_API_KEY.", null);
        }
        try {
            ChatResponse response = restClient.post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ChatRequest(
                            model,
                            List.of(new Message("system", systemPrompt), new Message("user", userPrompt)),
                            0.3,
                            maxTokens,
                            false))
                    .retrieve()
                    .body(ChatResponse.class);
            if (response == null || response.choices() == null || response.choices().isEmpty()
                    || response.choices().getFirst().message() == null
                    || response.choices().getFirst().message().content().isBlank()) {
                throw new TutorUnavailableException("DeepSeek a renvoyé une réponse vide.", null);
            }
            return response.choices().getFirst().message().content().strip();
        } catch (TutorUnavailableException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new TutorUnavailableException(
                    "Le professeur IA DeepSeek est indisponible. Le laboratoire reste utilisable sans IA.", exception);
        }
    }

    record ChatRequest(String model, List<Message> messages, double temperature, int max_tokens, boolean stream) {}
    record Message(String role, String content) {}
    record ChatResponse(List<Choice> choices) {}
    record Choice(Message message) {}
}
