package com.taskmanager.application.domain.assistent.exception;

import com.taskmanager.application.domain.shared.DomainException;

public class PromptIsEmptyException extends DomainException {
    public PromptIsEmptyException() {
        super("Prompt cannot be empty");
    }
}
