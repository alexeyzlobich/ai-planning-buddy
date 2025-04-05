package com.taskmanager.application.domain.user.valueobject;

import io.micronaut.core.util.StringUtils;
import jakarta.annotation.Nonnull;

public record UserId(@Nonnull String value) {

    @SuppressWarnings("ConstantValue")
    public UserId {
        value = (value == null) ? null : value.trim();
        if (StringUtils.isEmpty(value)) {
            throw new IllegalArgumentException("User ID cannot be empty");
        }
    }

    public static @Nonnull UserId from(@Nonnull String value) {
        return new UserId(value);
    }
}
