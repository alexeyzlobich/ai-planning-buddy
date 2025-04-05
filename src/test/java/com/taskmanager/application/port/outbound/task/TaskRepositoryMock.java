package com.taskmanager.application.port.outbound.task;

import com.taskmanager.application.domain.task.state.TaskState;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class TaskRepositoryMock {

    protected static final Map<String, TaskState> tasks = new LinkedHashMap<>(); // must be shared between command and query repos

    protected static void resetRepositoryState() {
        tasks.clear();
    }

}
