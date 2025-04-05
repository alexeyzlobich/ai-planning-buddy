package com.taskmanager.application.port.outbound.task;

import com.taskmanager.application.domain.task.Task;
import com.taskmanager.application.domain.task.valueobject.TaskStatus;
import com.taskmanager.application.domain.task.valueobject.TaskId;
import io.micronaut.core.annotation.NonNull;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for read operations on tasks.
 */
public interface TaskQueryRepository {

    /**
     * Finds a task by its ID.
     *
     * @param id the ID of the task
     * @return an Optional containing the found task, or empty if no task was found
     */
    @NonNull
    Optional<Task> findById(@NonNull TaskId id);

    /**
     * Retrieves all tasks from the repository.
     *
     * @return a list of all tasks
     */
    @NonNull
    List<Task> findAll();

    /**
     * Finds tasks by status.
     *
     * @param status the status to search for
     * @return a list of tasks with the specified status
     */
    @NonNull
    List<Task> findByStatus(@NonNull TaskStatus status);
}