package com.taskmanager.application.port.inbound.task.impl;

import com.taskmanager.application.domain.task.exception.TaskNotFoundException;
import com.taskmanager.application.domain.task.valueobject.TaskId;
import com.taskmanager.application.port.inbound.task.TaskQueryHandler;
import com.taskmanager.application.port.inbound.task.data.TaskData;
import com.taskmanager.application.port.inbound.task.mapper.TaskDataMapper;
import com.taskmanager.application.port.inbound.task.query.FindTaskByIdQuery;
import com.taskmanager.application.port.inbound.task.query.GetAvailableTasksQuery;
import com.taskmanager.application.port.outbound.task.TaskQueryRepository;
import io.micronaut.core.annotation.NonNull;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import java.util.List;

@Singleton
@RequiredArgsConstructor
public class TaskQueryHandlerImpl implements TaskQueryHandler {

    private final TaskQueryRepository taskQueryRepository;
    private final TaskDataMapper taskDataMapper;

    @Override
    public @NonNull TaskData handle(@Nonnull FindTaskByIdQuery query) {
        TaskId id = TaskId.from(query.taskId());
        return taskQueryRepository.findById(id)
                .map(taskDataMapper::toTaskData)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    @Override
    public @NonNull List<TaskData> handle(@Nonnull GetAvailableTasksQuery query) {
        return taskQueryRepository.findAll()
                .stream()
                .map(taskDataMapper::toTaskData)
                .toList();
    }
}
