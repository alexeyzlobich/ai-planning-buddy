package com.taskmanager.api.rest;

import com.taskmanager.api.rest.data.*;
import com.taskmanager.application.domain.assistent.exception.PromptIsEmptyException;
import com.taskmanager.application.domain.shared.DomainException;
import com.taskmanager.application.domain.task.exception.InvalidTaskIdException;
import com.taskmanager.application.domain.task.exception.TaskNotFoundException;
import com.taskmanager.application.port.inbound.assistent.AssistantQueryHandler;
import com.taskmanager.application.port.inbound.assistent.query.AskQuery;
import com.taskmanager.application.port.inbound.task.TaskCommandHandler;
import com.taskmanager.application.port.inbound.task.TaskQueryHandler;
import com.taskmanager.application.port.inbound.task.command.CompleteTaskCommand;
import com.taskmanager.application.port.inbound.task.command.CreateTaskCommand;
import com.taskmanager.application.port.inbound.task.command.UpdateTaskCommand;
import com.taskmanager.application.port.inbound.task.data.TaskData;
import com.taskmanager.application.port.inbound.task.query.FindTaskByIdQuery;
import com.taskmanager.application.port.inbound.task.query.GetAvailableTasksQuery;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller("/task-manager")
@Tag(name = "Task Manager API", description = "API for managing tasks and interacting with an assistant")
public class TaskManagerRestController {

    private final TaskCommandHandler taskCommandHandler;
    private final TaskQueryHandler taskQueryHandler;
    private final AssistantQueryHandler assistantQueryHandler;

    @Operation(summary = "Get all available tasks", description = "Retrieves a list of all available tasks")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved tasks",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = GetAllTasksResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string", example = "Something went wrong")))
    })
    @Get(uri = "/tasks", produces = MediaType.APPLICATION_JSON)
    public HttpResponse<?> getAllTasks() {
        try {
            List<TaskData> tasks = taskQueryHandler.handle(new GetAvailableTasksQuery());
            return HttpResponse.ok(new GetAllTasksResponse(tasks));
        } catch (RuntimeException e) {
            log.error("Error fetching all tasks", e);
            return HttpResponse.serverError("Something went wrong");
        }
    }

    @Operation(summary = "Create a new task", description = "Creates a new task with the provided details")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Task created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskData.class))),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string", example = "Something went wrong")))
    })
    @Post(uri = "/tasks", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    public HttpResponse<?> createTask(@Body CreateTaskRequest createTaskRequest) {
        try {
            TaskData createdTask = taskCommandHandler.handle(new CreateTaskCommand(createTaskRequest.title(), createTaskRequest.description()));
            return HttpResponse.created(createdTask);
        } catch (DomainException e) {
            log.debug("Error creating task by request [{}]", createTaskRequest, e);
            return HttpResponse.badRequest(e.getMessage());
        } catch (RuntimeException e) {
            log.error("Error creating task by request [{}]", createTaskRequest, e);
            return HttpResponse.serverError("Something went wrong");
        }
    }

    @Operation(summary = "Find task by ID", description = "Retrieves a task by its unique identifier")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskData.class))),
            @ApiResponse(responseCode = "400", description = "Invalid task ID format",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "404", description = "Task not found",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string", example = "Something went wrong")))
    })
    @Get(uri = "/tasks/{id}", produces = MediaType.APPLICATION_JSON)
    public HttpResponse<?> findTaskById(@Parameter(description = "The unique identifier of the task") @PathVariable("id") String id) {
        try {
            TaskData taskData = taskQueryHandler.handle(new FindTaskByIdQuery(id));
            return HttpResponse.ok(taskData);
        } catch (TaskNotFoundException e) {
            log.debug("Error finding task by id [{}]", id, e);
            return HttpResponse.notFound(e.getMessage());
        } catch (InvalidTaskIdException e) {
            log.debug("Error finding task by id [{}]", id, e);
            return HttpResponse.badRequest(e.getMessage());
        } catch (RuntimeException e) {
            log.error("Error finding task by id [{}]", id, e);
            return HttpResponse.serverError("Something went wrong");
        }
    }

    @Operation(summary = "Update a task", description = "Updates an existing task with the provided details")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskData.class))),
            @ApiResponse(responseCode = "400", description = "Bad request",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "404", description = "Task not found",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string", example = "Something went wrong")))
    })
    @Put(uri = "/tasks/{id}", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    public HttpResponse<?> updateTask(
            @Parameter(description = "The unique identifier of the task to update") @PathVariable("id") String id,
            @Body UpdateTaskRequest request) {
        try {
            UpdateTaskCommand command = new UpdateTaskCommand(id, request.title(), request.description());
            TaskData updatedTask = taskCommandHandler.handle(command);
            return HttpResponse.ok(updatedTask);
        } catch (TaskNotFoundException e) {
            log.debug("Error updating task by id [{}], request [{}]", id, request, e);
            return HttpResponse.notFound(e.getMessage());
        } catch (DomainException e) {
            log.debug("Error updating task by id [{}], request [{}]", id, request, e);
            return HttpResponse.badRequest(e.getMessage());
        } catch (RuntimeException e) {
            log.error("Error updating task by id [{}], request [{}]", id, request, e);
            return HttpResponse.serverError("Something went wrong");
        }
    }

    @Operation(summary = "Complete a task", description = "Marks a task as completed")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task completed successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskData.class))),
            @ApiResponse(responseCode = "400", description = "Invalid task ID format",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "404", description = "Task not found",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string", example = "Something went wrong")))
    })
    @Post(uri = "/tasks/{id}/complete", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    public HttpResponse<?> completeTask(@Parameter(description = "The unique identifier of the task to complete") @PathVariable("id") String id) {
        try {
            TaskData taskData = taskCommandHandler.handle(new CompleteTaskCommand(id));
            return HttpResponse.ok(taskData);
        } catch (TaskNotFoundException e) {
            log.debug("Error completing task by id [{}]", id, e);
            return HttpResponse.notFound(e.getMessage()); // TODO: Create object with standard error messages
        } catch (DomainException e) {
            log.debug("Error completing task by id [{}]", id, e);
            return HttpResponse.badRequest(e.getMessage());
        } catch (RuntimeException e) {
            log.error("Error completing task by id [{}]", id, e);
            return HttpResponse.serverError("Something went wrong");
        }
    }

    @Operation(summary = "Chat with assistant", description = "Send a message to the assistant and get a response")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Response received",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ChatResponse.class))),
            @ApiResponse(responseCode = "400", description = "Bad request (empty prompt)",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string", example = "Prompt cannot be empty"))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "text/plain", schema = @Schema(type = "string", example = "Something went wrong")))
    })
    @Post(uri = "/chat", consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    public HttpResponse<?> chat(@Body ChatRequest request) {
        try {
            String response = assistantQueryHandler.handle(new AskQuery(request.message()));
            return HttpResponse.ok(new ChatResponse(response));
        } catch (PromptIsEmptyException e) {
            log.debug("Error processing chat request", e);
            return HttpResponse.badRequest(e.getMessage());
        } catch (RuntimeException e) {
            log.error("Error processing chat request", e);
            return HttpResponse.serverError("Something went wrong");
        }
    }
}