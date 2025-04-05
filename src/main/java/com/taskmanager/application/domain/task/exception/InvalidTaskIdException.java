package com.taskmanager.application.domain.task.exception;

import com.taskmanager.application.domain.shared.DomainException;

public class InvalidTaskIdException extends DomainException {
    public InvalidTaskIdException(String message) {
        super(message);
    }
}
