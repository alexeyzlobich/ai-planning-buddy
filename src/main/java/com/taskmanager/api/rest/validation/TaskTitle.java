package com.taskmanager.api.rest.validation;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

@NotEmpty
@Size(max = 100)
public @interface TaskTitle {
}
