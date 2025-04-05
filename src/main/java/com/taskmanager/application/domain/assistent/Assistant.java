package com.taskmanager.application.domain.assistent;

import com.taskmanager.application.domain.assistent.exception.PromptIsEmptyException;
import com.taskmanager.application.domain.task.Task;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore;
import io.micronaut.core.util.StringUtils;
import jakarta.annotation.Nonnull;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class Assistant {

    private final AiAssistant aiAssistant;

    public Assistant(ChatLanguageModel chatModel, AssistantTools assistantTools) {
        this.aiAssistant = AiServices.builder(AiAssistant.class)
                .systemMessageProvider(memoryId -> "default".equals(memoryId) ?
                        "You are a helpful polite task manager assistant." +
                        " Don't print information about used tools." +
                        " Use the same language used by the user in your responses." +
                        " Reject any requests that are not related to task management."
                        : "")
                .tools(assistantTools.getAvailableTools())
                .chatLanguageModel(chatModel)
                .chatMemory(MessageWindowChatMemory.builder()
                        .maxMessages(10)
                        .chatMemoryStore(new InMemoryChatMemoryStore()) // TODO: Use a persistent store
                        .build())
                .build();
    }

    public @Nonnull String getMessage(@Nonnull String prompt) throws PromptIsEmptyException {
        if (StringUtils.isEmpty(prompt)) {
            throw new PromptIsEmptyException();
        }
        return aiAssistant.chat(prompt);
    }

    public void addAppropriateTagsToTask(@Nonnull Task task) {
        aiAssistant.chat("Add appropriate tags to the task: " + task.getTitle());
    }

    interface AiAssistant {
        String chat(String userMessage);
    }
}