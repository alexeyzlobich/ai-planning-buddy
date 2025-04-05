package com.taskmanager.application.port.outbound.task;

import com.taskmanager.application.domain.task.Task;
import com.taskmanager.application.domain.task.valueobject.TaskId;
import io.micronaut.core.annotation.NonNull;

import java.util.Optional;

/**
 * Repository interface for write operations on tasks.
 */
public interface TaskCommandRepository {

    /**
     * Finds a task by its ID.
     *
     * @param id the ID of the task
     * @return an Optional containing the found task, or empty if no task was found
     */
    @NonNull
    Optional<Task> findById(@NonNull TaskId id);

    /**
     * Saves a task to the repository.
     *
     * @param task the task to save
     * @return the saved task
     */
    @NonNull
    Task save(@NonNull Task task);

    /**
     * Deletes a task from the repository.
     *
     * @param task the task to delete
     */
    void delete(@NonNull Task task);
}