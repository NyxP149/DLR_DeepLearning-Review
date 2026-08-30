package com.dlr.shared.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HomeController {
    @GetMapping("/")
    public Map<String, String> home() {
        return Map.of(
                "application", "DLR — Deep Learning & Review",
                "status", "API disponible",
                "frontend", "http://localhost:4200",
                "swagger", "http://localhost:8081/swagger-ui.html",
                "health", "http://localhost:8081/actuator/health");
    }
}
