package com.taskmanager.application.domain.task.valueobject;

import com.taskmanager.application.domain.task.exception.InvalidTaskTitleException;
import io.micronaut.core.annotation.NonNull;

public record TaskTitle(@NonNull String value) {

    @SuppressWarnings("ConstantValue")
    public TaskTitle {
        value = (value == null) ? null : value.trim();

        if (value == null || value.isEmpty()) {
            throw new InvalidTaskTitleException("Title cannot be empty");
        }
        if (value.length() > 100) {
            throw new InvalidTaskTitleException("Title cannot be longer than 100 characters");
        }
    }

    public static @NonNull TaskTitle from(@NonNull String value) throws InvalidTaskTitleException {
        return new TaskTitle(value);
    }
}
