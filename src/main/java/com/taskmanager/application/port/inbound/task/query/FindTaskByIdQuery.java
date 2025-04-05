package com.taskmanager.application.port.inbound.task.query;

import javax.annotation.Nonnull;

public record FindTaskByIdQuery(@Nonnull String taskId) {
}
