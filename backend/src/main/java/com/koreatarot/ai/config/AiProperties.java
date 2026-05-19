package com.koreatarot.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai")
public record AiProperties(
        String baseUrl,
        String streamPath,
        long timeoutSeconds
) {
}
