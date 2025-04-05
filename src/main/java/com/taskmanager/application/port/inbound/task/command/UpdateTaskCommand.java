package com.taskmanager.application.port.inbound.task.command;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public record UpdateTaskCommand(@Nonnull String taskId,
                                @Nonnull String title,
                                @Nullable String description) {
}

