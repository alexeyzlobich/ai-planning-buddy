package com.taskmanager.application.domain.task.valueobject;

import com.taskmanager.application.domain.task.exception.InvalidTaskIdException;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.StringUtils;

public record TaskId(@NonNull String value) {

    @SuppressWarnings("ConstantValue")
    public TaskId {
        value = (value == null) ? null : value.trim();
        if (StringUtils.isEmpty(value)) {
            throw new InvalidTaskIdException("Task ID cannot be empty");
        }
    }

    public static @NonNull TaskId from(@NonNull String value) throws InvalidTaskIdException {
        return new TaskId(value);
    }
}