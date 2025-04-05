package com.taskmanager.application.port.inbound.task.mapper;

import com.taskmanager.application.domain.task.Task;
import com.taskmanager.application.port.inbound.task.data.TaskData;
import io.micronaut.core.annotation.NonNull;
import jakarta.inject.Singleton;

import java.util.Objects;

;

@Singleton
public class TaskDataMapper {

    public @NonNull TaskData toTaskData(@NonNull Task task) {
        return new TaskData(
                Objects.requireNonNull(task.getId()).value(),
                task.getTitle().value(),
                task.getDescription().value(),
                task.isCompleted()
        );
    }

}
