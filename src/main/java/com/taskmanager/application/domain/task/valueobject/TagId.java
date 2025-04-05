package com.taskmanager.application.domain.task.valueobject;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.StringUtils;

public record TagId(@NonNull String value) {

    @SuppressWarnings("ConstantValue")
    public TagId {
        value = (value == null) ? null : value.trim();
        if (StringUtils.isEmpty(value)) {
            throw new IllegalArgumentException("Tag ID cannot be empty");
        }
    }

    public static @NonNull TagId from(@NonNull String value) {
        return new TagId(value);
    }
}
