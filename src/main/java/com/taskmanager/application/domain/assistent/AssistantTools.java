package com.taskmanager.application.domain.assistent;

import com.taskmanager.application.domain.task.Task;
import com.taskmanager.application.domain.task.valueobject.TaskStatus;
import com.taskmanager.application.port.outbound.task.TaskQueryRepository;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.service.tool.ToolExecutor;
import jakarta.annotation.Nonnull;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Singleton
@RequiredArgsConstructor
public class AssistantTools {

    private final TaskQueryRepository taskQueryRepository;

    public @Nonnull Map<ToolSpecification, ToolExecutor> getAvailableTools() {
        Map<ToolSpecification, ToolExecutor> availableTools = new HashMap<>();

        ToolSpecification getTasksSpec = ToolSpecification.builder()
                .name("get-tasks")
                .description("Returns information about tasks with TODO status.")
                .build();
        availableTools.put(getTasksSpec, (toolExecutionRequest, memoryId) -> this.getTasksWithTodoStatus());

        return availableTools;
    }

    private @Nonnull String getTasksWithTodoStatus() {
        List<Task> tasks = taskQueryRepository.findByStatus(TaskStatus.TODO);

        StringBuilder tasksMessage = new StringBuilder();
        tasksMessage.append("<tasks>");
        tasks.forEach(task -> tasksMessage.append("<task>")
                .append("<id>").append(Objects.requireNonNull(task.getId()).value()).append("</id>")
                .append("<title>").append(task.getTitle()).append("</title>")
                .append("<description>").append(task.getDescription()).append("</description>")
                .append("</task>")
        );
        tasksMessage.append("</tasks>");

        return tasksMessage.toString();
    }

}
