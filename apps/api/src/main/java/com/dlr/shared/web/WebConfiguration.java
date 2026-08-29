package com.dlr.shared.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    private final String[] allowedOrigins;

    public WebConfiguration(@Value("${dlr.web.allowed-origins:http://localhost:4200,http://127.0.0.1:4200}") String origins) {
        this.allowedOrigins = Arrays.stream(origins.split(","))
                .map(String::strip)
                .filter(origin -> !origin.isBlank())
                .toArray(String[]::new);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
    }
}
