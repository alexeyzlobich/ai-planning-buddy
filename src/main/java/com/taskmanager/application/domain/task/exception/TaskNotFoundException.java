package com.taskmanager.application.domain.task.exception;

import com.taskmanager.application.domain.shared.DomainException;
import com.taskmanager.application.domain.task.valueobject.TaskId;

public class TaskNotFoundException extends DomainException {

    public TaskNotFoundException(TaskId taskId) {
        super("Task with id " + taskId.value() + " not found");
    }
}
