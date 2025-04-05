package com.taskmanager.application.port.outbound.task;

import com.taskmanager.application.domain.task.Task;
import com.taskmanager.application.domain.task.state.TaskState;
import com.taskmanager.application.domain.task.valueobject.TaskId;
import io.micronaut.core.annotation.NonNull;
import org.bson.types.ObjectId;

import java.util.Optional;

public class TaskCommandRepositoryMock extends TaskRepositoryMock implements TaskCommandRepository {

    public TaskCommandRepositoryMock() {
        resetRepositoryState();
    }

    @Override
    public @NonNull Optional<Task> findById(@NonNull TaskId id) {
        return Optional.ofNullable(tasks.get(id.value()))
                .map(Task::fromState);
    }

    @Override
    public @NonNull Task save(@NonNull Task task) {
        TaskState state = task.toState();
        if (state.id() == null) {
            state = TaskState.copy(state)
                    .id(new ObjectId().toHexString())
                    .build();
        }
        tasks.put(state.id(), state);
        return Task.fromState(state);
    }

    @Override
    public void delete(@NonNull Task task) {
        TaskState state = task.toState();
        tasks.remove(state.id());
    }

}
