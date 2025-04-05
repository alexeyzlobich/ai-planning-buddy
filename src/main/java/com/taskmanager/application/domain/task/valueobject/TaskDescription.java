package com.taskmanager.application.domain.task.valueobject;

import com.taskmanager.application.domain.task.exception.InvalidTaskDescriptionException;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public record TaskDescription(@Nullable String value) { // TODO: make not null + update repository

    public TaskDescription {
        if (value != null) {
            value = value.trim();
            if (value.length() > 1000) {
                throw new InvalidTaskDescriptionException("Description cannot be longer than 1000 characters");
            }
        }
    }

    public static @Nonnull TaskDescription from(@Nullable String value) throws InvalidTaskDescriptionException {
        return new TaskDescription(value);
    }
}