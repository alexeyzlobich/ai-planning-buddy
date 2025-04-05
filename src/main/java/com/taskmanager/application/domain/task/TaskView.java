package com.taskmanager.application.domain.task;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;

public record TaskView(
        @NonNull String id,
        @NonNull String title,
        @Nullable String description,
        boolean completed
) {
}
