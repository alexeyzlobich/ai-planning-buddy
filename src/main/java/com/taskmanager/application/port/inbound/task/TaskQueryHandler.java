package com.taskmanager.application.port.inbound.task;

import com.taskmanager.application.domain.task.exception.InvalidTaskIdException;
import com.taskmanager.application.domain.task.exception.TaskNotFoundException;
import com.taskmanager.application.port.inbound.task.data.TaskData;
import com.taskmanager.application.port.inbound.task.query.FindTaskByIdQuery;
import com.taskmanager.application.port.inbound.task.query.GetAvailableTasksQuery;

import javax.annotation.Nonnull;
import java.util.List;

public interface TaskQueryHandler {

    /**
     * Handles the query to find a task by its ID.
     *
     * @param query the query containing the task ID
     * @return the task data
     * @throws TaskNotFoundException  if the task is not found
     * @throws InvalidTaskIdException if the task ID is invalid
     */
    @Nonnull
    TaskData handle(@Nonnull FindTaskByIdQuery query);

    /**
     * Handles the query to get all available tasks.
     *
     * @param query the query to get available tasks
     * @return the list of task data
     */
    @Nonnull
    List<TaskData> handle(@Nonnull GetAvailableTasksQuery query);
}
