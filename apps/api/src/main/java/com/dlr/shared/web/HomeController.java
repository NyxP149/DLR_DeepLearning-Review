package com.dlr.shared.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HomeController {
    private final String frontendUrl;
    private final String publicApiUrl;

    public HomeController(
            @Value("${dlr.web.frontend-url:http://localhost:4200}") String frontendUrl,
            @Value("${dlr.web.public-api-url:http://localhost:8081}") String publicApiUrl
    ) {
        this.frontendUrl = withoutTrailingSlash(frontendUrl);
        this.publicApiUrl = withoutTrailingSlash(publicApiUrl);
    }

    @GetMapping("/")
    public Map<String, String> home() {
        return Map.of(
                "application", "DLR — Deep Learning & Review",
                "status", "API disponible",
                "frontend", frontendUrl,
                "swagger", publicApiUrl + "/swagger-ui.html",
                "health", publicApiUrl + "/actuator/health");
    }

    private static String withoutTrailingSlash(String value) {
        return value.replaceAll("/+$", "");
    }
}
