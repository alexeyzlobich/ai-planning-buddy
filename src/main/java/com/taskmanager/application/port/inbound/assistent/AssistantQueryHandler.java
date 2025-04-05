package com.taskmanager.application.port.inbound.assistent;

import com.taskmanager.application.domain.assistent.exception.PromptIsEmptyException;
import com.taskmanager.application.port.inbound.assistent.query.AskQuery;
import io.micronaut.core.annotation.NonNull;

public interface AssistantQueryHandler {

    /**
     * Handles the query to ask the assistant a question.
     *
     * @param query the query containing the prompt
     * @return the response from the assistant
     * @throws PromptIsEmptyException if the prompt is null or empty
     */
    @NonNull
    String handle(@NonNull AskQuery query) throws PromptIsEmptyException;
}
