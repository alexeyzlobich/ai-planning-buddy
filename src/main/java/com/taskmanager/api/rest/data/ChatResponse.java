package com.taskmanager.api.rest.data;

import io.micronaut.serde.annotation.Serdeable;

import javax.annotation.Nonnull;

@Serdeable
public record ChatResponse(@Nonnull String message) {
}
