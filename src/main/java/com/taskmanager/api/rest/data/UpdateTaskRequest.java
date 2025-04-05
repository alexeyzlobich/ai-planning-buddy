package com.taskmanager.api.rest.data;

import com.taskmanager.api.rest.validation.TaskDescription;
import com.taskmanager.api.rest.validation.TaskTitle;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record UpdateTaskRequest(@TaskTitle String title,
                                @TaskDescription String description) {
}
