package com.taskmanager.api.rest.data;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotEmpty;

@Serdeable
public record ChatRequest(@NotEmpty String message) { //TODO: add validation
}
