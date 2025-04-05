package com.taskmanager.application.domain.task.event;

public record TaskCreatedEvent(
        String taskId,
        String description
) {
}
