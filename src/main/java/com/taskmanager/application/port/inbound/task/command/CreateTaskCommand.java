package com.taskmanager.application.port.inbound.task.command;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public record CreateTaskCommand(@Nonnull String title, @Nullable String description) {
}
