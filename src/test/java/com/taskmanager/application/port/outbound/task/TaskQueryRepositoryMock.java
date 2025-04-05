package com.taskmanager.application.port.outbound.task;

import com.taskmanager.application.domain.task.Task;
import com.taskmanager.application.domain.task.valueobject.TaskStatus;
import com.taskmanager.application.domain.task.valueobject.TaskId;
import io.micronaut.core.annotation.NonNull;

import java.util.List;
import java.util.Optional;

public class TaskQueryRepositoryMock extends TaskRepositoryMock implements TaskQueryRepository {

    @Override
    public @NonNull Optional<Task> findById(@NonNull TaskId id) {
        return Optional.ofNullable(tasks.get(id.value()))
                .map(Task::fromState);
    }

    @Override
    public @NonNull List<Task> findAll() {
        return tasks.values().stream()
                .map(Task::fromState)
                .toList();
    }

    @Override
    public @NonNull List<Task> findByStatus(@NonNull TaskStatus status) {
        return tasks.values().stream()
                .filter(task -> task.status().equals(status.name()))
                .map(Task::fromState)
                .toList();
    }

}
