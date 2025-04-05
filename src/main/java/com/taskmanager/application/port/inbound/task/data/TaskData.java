package com.taskmanager.application.port.inbound.task.data;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record TaskData(@NonNull String id,
                       @NonNull String title,
                       @Nullable String description,
                       boolean completed) {
}