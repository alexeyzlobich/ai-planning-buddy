package com.taskmanager.application.port.inbound.assistent.impl;

import com.taskmanager.application.domain.assistent.exception.PromptIsEmptyException;
import com.taskmanager.application.port.inbound.assistent.AssistantQueryHandler;
import com.taskmanager.application.port.inbound.assistent.query.AskQuery;
import com.taskmanager.application.port.outbound.asistance.ChatLanguageModelMock;
import dev.langchain4j.model.chat.ChatLanguageModel;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;

@MicronautTest(startApplication = false)
@DisplayName("Assistant Query Use Cases")
class AssistantQueryHandlerImplTest {

    @Inject
    AssistantQueryHandler assistantQueryHandler;

    @Nested
    @DisplayName("Use case: Ask Assistant")
    class AskQueryUseCase {

        @Test
        @DisplayName("should return response from assistant")
        void shouldReturnResponseFromAssistant() {
            // given
            String prompt = "Hello";

            // when
            String actualResult = assistantQueryHandler.handle(new AskQuery(prompt));

            // then
            assertThat(actualResult).isEqualTo("Hello, I'm Stub. How can I help you?");
        }

        @Test
        @DisplayName("should handle long prompts")
        void shouldHandleLongPrompts() {
            // given
            String longPrompt = "a".repeat(1000);

            // when
            String actualResult = assistantQueryHandler.handle(new AskQuery(longPrompt));

            // then
            assertThat(actualResult).isEqualTo("Hello, I'm Stub. How can I help you?");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("should handle empty prompts")
        void shouldHandleEmptyPrompts(String emptyPrompt) {
            // when
            Exception exception = catchException(() -> assistantQueryHandler.handle(new AskQuery(emptyPrompt)));

            // then
            assertThat(exception)
                    .isInstanceOf(PromptIsEmptyException.class)
                    .hasMessageContaining("Prompt cannot be empty");
        }
    }

    @MockBean(ChatLanguageModel.class)
    ChatLanguageModel chatLanguageModel() {
        return new ChatLanguageModelMock();
    }
}