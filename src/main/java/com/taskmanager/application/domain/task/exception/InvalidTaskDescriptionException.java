package com.taskmanager.application.domain.task.exception;

import com.taskmanager.application.domain.shared.DomainException;

public class InvalidTaskDescriptionException extends DomainException {
    public InvalidTaskDescriptionException(String message) {
        super(message);
    }
}
