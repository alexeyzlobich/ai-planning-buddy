package com.taskmanager.api.rest.validation;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Size;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Nullable
@Size(max = 1000)
@Retention(RUNTIME)
public @interface TaskDescription {
}
