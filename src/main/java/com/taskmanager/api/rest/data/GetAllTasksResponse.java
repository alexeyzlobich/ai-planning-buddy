package com.taskmanager.api.rest.data;

import com.taskmanager.application.port.inbound.task.data.TaskData;
import io.micronaut.serde.annotation.Serdeable;

import javax.annotation.Nullable;
import java.util.List;

@Serdeable
public record GetAllTasksResponse(@Nullable List<TaskData> tasks) {
}
