package com.taskmanager.application.domain.shared;

import jakarta.annotation.Nullable;

public interface Entity<T> {

    @Nullable // in a case if entity is not yet saved
    T getId();

}
