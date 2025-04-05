package com.taskmanager.application.domain.task.exception;

import com.taskmanager.application.domain.shared.DomainException;

public class InvalidTaskTitleException extends DomainException {
    public InvalidTaskTitleException(String message) {
        super(message);
    }
}
