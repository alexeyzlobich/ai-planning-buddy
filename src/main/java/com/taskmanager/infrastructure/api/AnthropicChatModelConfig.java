package com.taskmanager.infrastructure.api;

import dev.langchain4j.model.anthropic.AnthropicChatModel;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;

@Factory
public class AnthropicChatModelConfig {

    @Value("${anthropic.api-key}")
    private String apiKey;

    @Singleton
    public AnthropicChatModel chatModel() {
        return AnthropicChatModel.builder()
                .apiKey(apiKey)
                .modelName("claude-3-haiku-20240307")
                .maxTokens(1024)
                .temperature(0.0)
                .build();
    }

}
