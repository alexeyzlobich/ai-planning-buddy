package com.taskmanager.application.port.inbound.task.command;

import javax.annotation.Nonnull;

public record CompleteTaskCommand(@Nonnull String taskId) {
}
