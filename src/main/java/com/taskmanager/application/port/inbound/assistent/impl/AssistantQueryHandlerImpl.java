package com.taskmanager.application.port.inbound.assistent.impl;

import com.taskmanager.application.domain.assistent.Assistant;
import com.taskmanager.application.domain.assistent.exception.PromptIsEmptyException;
import com.taskmanager.application.port.inbound.assistent.AssistantQueryHandler;
import com.taskmanager.application.port.inbound.assistent.query.AskQuery;
import jakarta.annotation.Nonnull;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor
public class AssistantQueryHandlerImpl implements AssistantQueryHandler {

    private final Assistant assistant;

    @Override
    public @Nonnull String handle(@Nonnull AskQuery query) throws PromptIsEmptyException {
        return assistant.getMessage(query.prompt());
    }
}
