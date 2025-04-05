package com.taskmanager.infrastructure.persistence.mapper;

import com.taskmanager.application.domain.task.state.TaskState;
import com.taskmanager.infrastructure.persistence.entity.TaskEntity;
import org.bson.types.ObjectId;

import java.util.Optional;

public class TaskEntityMapper {

    public TaskEntity convertToEntity(TaskState taskState) {
        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setId(Optional.ofNullable(taskState.id())
                .map(ObjectId::new)
                .orElse(null));
        taskEntity.setUserId(taskState.userId());
        taskEntity.setTitle(taskState.title());
        taskEntity.setDescription(taskState.description());
        taskEntity.setStatus(taskState.status());
        return taskEntity;
    }

    public TaskState convertToDomain(TaskEntity taskEntity) {
        return TaskState.builder()
                .id(taskEntity.getId().toHexString())
                .userId(taskEntity.getUserId())
                .title(taskEntity.getTitle())
                .description(taskEntity.getDescription())
                .status(taskEntity.getStatus())
                .build();
    }

}
