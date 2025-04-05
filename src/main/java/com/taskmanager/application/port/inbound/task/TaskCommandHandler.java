package com.taskmanager.application.port.inbound.task;

import com.taskmanager.application.domain.task.exception.InvalidTaskDescriptionException;
import com.taskmanager.application.domain.task.exception.InvalidTaskIdException;
import com.taskmanager.application.domain.task.exception.InvalidTaskTitleException;
import com.taskmanager.application.domain.task.exception.TaskNotFoundException;
import com.taskmanager.application.port.inbound.task.command.CompleteTaskCommand;
import com.taskmanager.application.port.inbound.task.command.CreateTaskCommand;
import com.taskmanager.application.port.inbound.task.command.UpdateTaskCommand;
import com.taskmanager.application.port.inbound.task.data.TaskData;

import javax.annotation.Nonnull;


public interface TaskCommandHandler {

    /**
     * Handles the creation of a new task.
     *
     * @param command the command containing task details
     * @return the created task data
     * @throws InvalidTaskTitleException       if the task title is invalid
     * @throws InvalidTaskDescriptionException if the task description is invalid
     */
    @Nonnull
    TaskData handle(@Nonnull CreateTaskCommand command);

    /**
     * Handles the update of an existing task's description.
     *
     * @param command the command containing the task ID and new description
     * @return the updated task data
     * @throws TaskNotFoundException           if the task is not found
     * @throws InvalidTaskIdException          if the task ID is invalid
     * @throws InvalidTaskTitleException       if the task title is invalid
     * @throws InvalidTaskDescriptionException if the task description is invalid
     */
    @Nonnull
    TaskData handle(@Nonnull UpdateTaskCommand command);

    /**
     * Handles the completion of a task.
     *
     * @param command the command containing the task ID
     * @return the completed task data
     * @throws InvalidTaskIdException if the task ID is invalid
     * @throws TaskNotFoundException  if the task is not found
     */
    @Nonnull
    TaskData handle(@Nonnull CompleteTaskCommand command);

    // TODO: add remove command
}
