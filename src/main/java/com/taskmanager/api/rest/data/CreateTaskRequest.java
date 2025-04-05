package com.taskmanager.api.rest.data;

import com.taskmanager.api.rest.validation.TaskDescription;
import com.taskmanager.api.rest.validation.TaskTitle;
import io.micronaut.serde.annotation.Serdeable;


// TODO: Add validation
@Serdeable
public record CreateTaskRequest(@TaskTitle String title,
                                @TaskDescription String description) {

}
