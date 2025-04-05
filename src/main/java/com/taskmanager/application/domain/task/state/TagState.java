package com.taskmanager.application.domain.task.state;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.Builder;

@Builder
public record TagState(
        @Nullable String id,
        @Nonnull String name,
        @Nullable String description
) {
}
