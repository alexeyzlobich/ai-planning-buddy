package com.taskmanager.application.domain.task.state;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.Builder;

import java.util.List;

@Builder
public record TaskState(
        @Nullable String id,
        @Nonnull String userId,
        @Nonnull String title,
        @Nullable String description,
        @Nonnull String status,
        @Nullable List<String> tags
) {

    public static @Nonnull TaskStateBuilder copy(@Nonnull TaskState state) {
        return TaskState.builder()
                .id(state.id())
                .userId(state.userId())
                .title(state.title())
                .description(state.description())
                .status(state.status())
                .tags(state.tags());
    }
}
