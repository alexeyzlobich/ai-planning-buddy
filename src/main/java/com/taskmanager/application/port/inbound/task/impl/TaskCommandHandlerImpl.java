package com.taskmanager.application.port.inbound.task.impl;

import com.taskmanager.application.domain.task.Task;
import com.taskmanager.application.domain.task.exception.TaskNotFoundException;
import com.taskmanager.application.domain.task.valueobject.TaskDescription;
import com.taskmanager.application.domain.task.valueobject.TaskId;
import com.taskmanager.application.domain.task.valueobject.TaskTitle;
import com.taskmanager.application.domain.user.valueobject.UserId;
import com.taskmanager.application.port.inbound.task.TaskCommandHandler;
import com.taskmanager.application.port.inbound.task.command.CompleteTaskCommand;
import com.taskmanager.application.port.inbound.task.command.CreateTaskCommand;
import com.taskmanager.application.port.inbound.task.command.UpdateTaskCommand;
import com.taskmanager.application.port.inbound.task.data.TaskData;
import com.taskmanager.application.port.inbound.task.mapper.TaskDataMapper;
import com.taskmanager.application.port.outbound.task.TaskCommandRepository;
import io.micronaut.core.annotation.NonNull;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;

@Singleton
@RequiredArgsConstructor
public class TaskCommandHandlerImpl implements TaskCommandHandler {

    private final TaskCommandRepository taskCommandRepository;
    private final TaskDataMapper taskDataMapper;

    @Override
    public @NonNull TaskData handle(@Nonnull CreateTaskCommand command) {
        UserId userId = UserId.from("anonymous"); // TODO: replace with actual user ID
        TaskTitle taskTitle = TaskTitle.from(command.title());
        TaskDescription taskDescription = TaskDescription.from(command.description());

        Task task = new Task(taskTitle, userId);
        task.setDescription(taskDescription);

        task = taskCommandRepository.save(task);
        return taskDataMapper.toTaskData(task);
    }

    @Override
    public @NonNull TaskData handle(@NonNull UpdateTaskCommand command) {
        TaskId id = TaskId.from(command.taskId());
        TaskTitle taskTitle = TaskTitle.from(command.title());
        TaskDescription taskDescription = TaskDescription.from(command.description());

        Task task = taskCommandRepository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
        task.setTitle(taskTitle);
        task.setDescription(taskDescription);

        task = taskCommandRepository.save(task);
        return taskDataMapper.toTaskData(task);
    }

    @Override
    public @Nonnull TaskData handle(@Nonnull CompleteTaskCommand command) {
        TaskId id = TaskId.from(command.taskId());

        Task task = taskCommandRepository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
        task.markComplete();

        task = taskCommandRepository.save(task);
        return taskDataMapper.toTaskData(task);
    }
}