package com.taskmanager.infrastructure.persistence;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.taskmanager.application.domain.task.Task;
import com.taskmanager.application.domain.task.valueobject.TaskStatus;
import com.taskmanager.application.domain.task.valueobject.TaskId;
import com.taskmanager.application.port.outbound.task.TaskCommandRepository;
import com.taskmanager.application.port.outbound.task.TaskQueryRepository;
import com.taskmanager.infrastructure.persistence.entity.TaskEntity;
import com.taskmanager.infrastructure.persistence.mapper.TaskEntityMapper;
import io.micronaut.core.annotation.NonNull;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.mongodb.client.model.Filters.eq;

@Slf4j
@Singleton
public class MongoDbTaskRepositoryAdapter implements TaskCommandRepository, TaskQueryRepository {

    private final MongoCollection<TaskEntity> taskCollection;
    private final TaskEntityMapper taskEntityMapper;

    private static class Field {
        private static final String ID = "_id";
        private static final String STATUS = "status";
    }

    public MongoDbTaskRepositoryAdapter(MongoClient mongoClient) {
        this.taskCollection = mongoClient
                .getDatabase("task-manager")
                .getCollection("tasks", TaskEntity.class);
        this.taskEntityMapper = new TaskEntityMapper();
    }

    @Override
    public @NonNull Task save(@NonNull Task task) {
        TaskEntity taskEntity = taskEntityMapper.convertToEntity(task.toState());
        ObjectId entityId = taskEntity.getId();
        if (entityId == null) {
            taskCollection.insertOne(taskEntity);
            // TODO: handle mongo exceptions
        } else {
            // no need to use findAndModify here, as we are using replaceOne that updates the whole state
            taskCollection.replaceOne(eq(Field.ID, entityId), taskEntity);
        }
        return Task.fromState(taskEntityMapper.convertToDomain(taskEntity));
    }

    @Override
    public void delete(@NonNull Task task) {
        TaskEntity taskEntity = taskEntityMapper.convertToEntity(task.toState());
        taskCollection.deleteOne(eq(Field.ID, taskEntity.getId()));
    }

    @Override
    public @NonNull Optional<Task> findById(@NonNull TaskId id) {
        TaskEntity taskState = taskCollection.find(eq(Field.ID, new ObjectId(id.value()))).first();
        return Optional.ofNullable(taskState)
                .map(taskEntityMapper::convertToDomain)
                .map(Task::fromState);
    }

    @Override
    public @NonNull List<Task> findAll() {
        FindIterable<TaskEntity> entities = taskCollection.find();
        return StreamSupport.stream(entities.spliterator(), false)
                .map(taskEntityMapper::convertToDomain)
                .map(Task::fromState)
                .collect(Collectors.toList());
    }

    @Override
    public @NonNull List<Task> findByStatus(@NonNull TaskStatus status) {
        FindIterable<TaskEntity> entities = taskCollection.find(eq(Field.STATUS, status.name()));
        return StreamSupport.stream(entities.spliterator(), false)
                .map(taskEntityMapper::convertToDomain)
                .map(Task::fromState)
                .collect(Collectors.toList());
    }
}
