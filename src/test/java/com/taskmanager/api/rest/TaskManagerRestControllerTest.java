package com.taskmanager.api.rest;

import com.taskmanager.api.rest.data.*;
import com.taskmanager.application.domain.assistent.exception.PromptIsEmptyException;
import com.taskmanager.application.domain.task.exception.InvalidTaskDescriptionException;
import com.taskmanager.application.domain.task.exception.InvalidTaskIdException;
import com.taskmanager.application.domain.task.exception.InvalidTaskTitleException;
import com.taskmanager.application.domain.task.exception.TaskNotFoundException;
import com.taskmanager.application.domain.task.valueobject.TaskId;
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
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@MicronautTest
@DisplayName("Rest API specification: Task Manager")
class TaskManagerRestControllerTest {

    @Inject
    @Client("/task-manager")
    HttpClient client;

    @Inject
    TaskQueryHandler taskQueryHandler;
    @Inject
    TaskCommandHandler taskCommandHandler;
    @Inject
    AssistantQueryHandler assistantQueryHandler;

    @Nested
    @DisplayName("'Get tasks' endpoint")
    class GetTasks {

        @Test
        @DisplayName("should return available tasks")
        void shouldReturnAvailableTasks() {
            // given
            TaskData taskData = Sample.taskData();
            given(taskQueryHandler.handle(new GetAvailableTasksQuery())).willReturn(List.of(taskData));

            // when
            HttpResponse<GetAllTasksResponse> response = Interaction.getTasks(client);

            // then
            assertThat(response.getStatus().getCode()).isEqualTo(200);
            assertThat(response.getBody()).hasValueSatisfying(body -> {
                assertThat(body.tasks()).hasSize(1);
                assertThat(body.tasks().getFirst()).usingRecursiveComparison().isEqualTo(taskData);
            });
        }

        @Test
        @DisplayName("should return empty list when no tasks")
        void shouldReturnEmptyList_whenNoTasks() {
            // given
            given(taskQueryHandler.handle(new GetAvailableTasksQuery())).willReturn(List.of());

            // when
            HttpResponse<GetAllTasksResponse> response = Interaction.getTasks(client);

            // then
            assertThat(response.getStatus().getCode()).isEqualTo(200);
            assertThat(response.getBody()).hasValueSatisfying(body -> {
                assertThat(body.tasks()).isNull();
            });
        }

        @Test
        @DisplayName("should return '500 internal server error' when unexpected error occurs")
        void shouldReturnInternalServerError_whenUnexpectedError() {
            // given
            given(taskQueryHandler.handle(new GetAvailableTasksQuery())).willThrow(new RuntimeException("Unexpected error"));

            // when
            Exception exception = catchException(() -> Interaction.getTasks(client));

            // then
            assertThat(exception).isInstanceOfSatisfying(HttpClientResponseException.class, e -> {
                assertThat(e.getStatus().getCode()).isEqualTo(500);
                assertThat(e.getResponse().getBody(String.class)).hasValue("Something went wrong");
            });
        }
    }

    @Nested
    @DisplayName("'Create new task' endpoint")
    class CreateNewTask {

        @Test
        void shouldCreateTaskSuccessfully() {
            // given
            CreateTaskRequest request = new CreateTaskRequest("Test task", "Task Description");

            TaskData createdTask = Sample.taskData();
            given(taskCommandHandler.handle(new CreateTaskCommand(request.title(), request.description()))).willReturn(createdTask);

            // when
            HttpResponse<TaskData> response = Interaction.createNewTask(client, request);

            // then
            assertThat(response.getStatus().getCode()).isEqualTo(201);
            assertThat(response.getBody()).hasValueSatisfying(body -> {
                assertThat(body).usingRecursiveComparison().isEqualTo(createdTask);
            });
        }

        @Test
        @DisplayName("should return '400 bad request' when title is invalid")
        void shouldReturnBadRequest_whenTitleInvalid() {
            // given
            CreateTaskRequest request = new CreateTaskRequest(null, "Description");

            given(taskCommandHandler.handle(new CreateTaskCommand(request.title(), request.description())))
                    .willThrow(new InvalidTaskTitleException("Title is invalid"));

            // when
            Exception exception = catchException(() -> Interaction.createNewTask(client, request));

            // then
            assertThat(exception).isInstanceOfSatisfying(HttpClientResponseException.class, e -> {
                assertThat(e.getStatus().getCode()).isEqualTo(400);
                assertThat(e.getResponse().getBody(String.class)).hasValue("Title is invalid");
            });
        }

        @Test
        @DisplayName("should return '400 bad request' when description is invalid")
        void shouldReturnBadRequest_whenDescriptionInvalid() {
            // given
            CreateTaskRequest request = new CreateTaskRequest("Title", "<invalid>");

            given(taskCommandHandler.handle(new CreateTaskCommand(request.title(), request.description())))
                    .willThrow(new InvalidTaskDescriptionException("Description is invalid"));

            // when
            Exception exception = catchException(() -> Interaction.createNewTask(client, request));

            // then
            assertThat(exception).isInstanceOfSatisfying(HttpClientResponseException.class, e -> {
                assertThat(e.getStatus().getCode()).isEqualTo(400);
                assertThat(e.getResponse().getBody(String.class)).hasValue("Description is invalid");
            });
        }

        @Test
        @DisplayName("should return '500 internal server error' when unexpected error occurs")
        void shouldReturnInternalServerError_whenUnexpectedError() {
            // given
            CreateTaskRequest request = new CreateTaskRequest("any", "any");
            given(taskCommandHandler.handle(any(CreateTaskCommand.class))).willThrow(new RuntimeException("Unexpected error"));

            // when
            Exception exception = catchException(() -> Interaction.createNewTask(client, request));

            // then
            assertThat(exception).isInstanceOfSatisfying(HttpClientResponseException.class, e -> {
                assertThat(e.getStatus().getCode()).isEqualTo(500);
                assertThat(e.getResponse().getBody(String.class)).hasValue("Something went wrong");
            });
        }

    }

    @Nested
    @DisplayName("'Find task by ID' endpoint")
    class FindTaskById {

        @Test
        @DisplayName("should return task when exists")
        void shouldReturnTask_whenExists() {
            // given
            String taskId = "00000000f6b5a229daa5525d";

            TaskData taskData = Sample.taskData();
            given(taskQueryHandler.handle(eq(new FindTaskByIdQuery(taskId)))).willReturn(taskData);

            // when
            HttpResponse<TaskData> response = Interaction.findTaskById(client, taskId);

            // then
            assertThat(response.getStatus().getCode()).isEqualTo(200);
            assertThat(response.getBody()).hasValueSatisfying(body -> {
                assertThat(body).usingRecursiveComparison().isEqualTo(taskData);
            });
        }

        @Test
        @DisplayName("should return '404 not found' when no task")
        void shouldReturnNotFound_whenNoTask() {
            // given
            String taskId = "00000000f6b5a229daa5525d";
            given(taskQueryHandler.handle(eq(new FindTaskByIdQuery(taskId)))).willThrow(new TaskNotFoundException(TaskId.from(taskId)));

            // when
            Exception exception = catchException(() -> Interaction.findTaskById(client, taskId));

            // then
            assertThat(exception).isInstanceOfSatisfying(HttpClientResponseException.class, e -> {
                assertThat(e.getStatus().getCode()).isEqualTo(404);
                assertThat(e.getResponse().getBody(String.class)).hasValue("Task with id 00000000f6b5a229daa5525d not found");
            });
        }

        @Test
        @DisplayName("should return '400 bad request' when task ID is invalid")
        void shouldReturnBadRequest_whenTaskIdInvalid() {
            // given
            String taskId = "invalid-id";
            given(taskQueryHandler.handle(eq(new FindTaskByIdQuery(taskId))))
                    .willThrow(new InvalidTaskIdException("Invalid task ID format"));

            // when
            Exception exception = catchException(() -> Interaction.findTaskById(client, taskId));

            // then
            assertThat(exception).isInstanceOfSatisfying(HttpClientResponseException.class, e -> {
                assertThat(e.getStatus().getCode()).isEqualTo(400);
                assertThat(e.getResponse().getBody(String.class)).hasValue("Invalid task ID format");
            });
        }

        @Test
        @DisplayName("should return '500 internal server error' when unexpected error occurs")
        void shouldReturnInternalServerError_whenUnexpectedError() {
            // given
            String taskId = "00000000f6b5a229daa5525d";
            given(taskQueryHandler.handle(eq(new FindTaskByIdQuery(taskId)))).willThrow(new RuntimeException("Unexpected error"));

            // when
            Exception exception = catchException(() -> Interaction.findTaskById(client, taskId));

            // then
            assertThat(exception).isInstanceOfSatisfying(HttpClientResponseException.class, e -> {
                assertThat(e.getStatus().getCode()).isEqualTo(500);
                assertThat(e.getResponse().getBody(String.class)).hasValue("Something went wrong");
            });
        }
    }

    @Nested
    @DisplayName("'Update task' endpoint")
    class UpdateTask {

        @Test
        @DisplayName("should update task successfully")
        void shouldUpdateTaskSuccessfully() {
            // given
            String taskId = "00000000f6b5a229daa5525d";
            String newTitle = "Updated Task Title";
            String newDescription = "Updated Task Description";

            TaskData updatedTask = Sample.taskData();
            given(taskCommandHandler.handle(new UpdateTaskCommand(taskId, newTitle, newDescription))).willReturn(updatedTask);

            // when
            HttpResponse<TaskData> response = Interaction.updateTask(client, taskId, new UpdateTaskRequest(newTitle, newDescription));

            // then
            assertThat(response.getStatus().getCode()).isEqualTo(200);
            assertThat(response.getBody()).hasValueSatisfying(body -> {
                assertThat(body).isEqualTo(updatedTask);
            });
        }

        @Test
        @DisplayName("should return '400 bad request' when description is invalid")
        void shouldReturnBadRequest_whenDescriptionInvalid() {
            // given
            String taskId = "00000000f6b5a229daa5525d";
            String title = "Task Title";
            String invalidDescription = "<invalid>";

            given(taskCommandHandler.handle(new UpdateTaskCommand(taskId, title, invalidDescription)))
                    .willThrow(new InvalidTaskDescriptionException("Invalid task description"));

            // when
            Exception exception = catchException(() -> Interaction.updateTask(client, taskId, new UpdateTaskRequest(title, invalidDescription)));

            // then
            assertThat(exception).isInstanceOfSatisfying(HttpClientResponseException.class, e -> {
                assertThat(e.getStatus().getCode()).isEqualTo(400);
                assertThat(e.getResponse().getBody(String.class)).hasValue("Invalid task description");
            });
        }

        @Test
        @DisplayName("should return '400 bad request' when task ID is invalid")
        void shouldReturnBadRequest_whenTaskIdInvalid() {
            // given
            String taskId = "invalid-id";
            given(taskCommandHandler.handle(any(UpdateTaskCommand.class))).willThrow(new InvalidTaskIdException("Invalid task ID format"));

            // when
            Exception exception = catchException(() -> Interaction.updateTask(client, taskId, new UpdateTaskRequest("any", "any")));

            // then
            assertThat(exception).isInstanceOfSatisfying(HttpClientResponseException.class, e -> {
                assertThat(e.getStatus().getCode()).isEqualTo(400);
                assertThat(e.getResponse().getBody(String.class)).hasValue("Invalid task ID format");
            });
        }

        @Test
        @DisplayName("should return '404 not found' when task not found")
        void shouldReturnNotFound_whenTaskNotFound() {
            // given
            String taskId = "00000000f6b5a229daa5525d";
            given(taskCommandHandler.handle(any(UpdateTaskCommand.class))).willThrow(new TaskNotFoundException(TaskId.from(taskId)));

            // when
            Exception exception = catchException(() -> Interaction.updateTask(client, taskId, new UpdateTaskRequest("any", "any")));

            // then
            assertThat(exception).isInstanceOfSatisfying(HttpClientResponseException.class, e -> {
                assertThat(e.getStatus().getCode()).isEqualTo(404);
                assertThat(e.getResponse().getBody(String.class)).hasValue("Task with id 00000000f6b5a229daa5525d not found");
            });
        }

        @Test
        @DisplayName("should return '500 internal server error' when unexpected error occurs")
        void shouldReturnInternalServerError_whenUnexpectedError() {
            // given
            String taskId = "00000000f6b5a229daa5525d";
            given(taskCommandHandler.handle(any(UpdateTaskCommand.class))).willThrow(new RuntimeException("Unexpected error"));

            // when
            Exception exception = catchException(() -> Interaction.updateTask(client, taskId, new UpdateTaskRequest("any", "any")));

            // then
            assertThat(exception).isInstanceOfSatisfying(HttpClientResponseException.class, e -> {
                assertThat(e.getStatus().getCode()).isEqualTo(500);
                assertThat(e.getResponse().getBody(String.class)).hasValue("Something went wrong");
            });
        }
    }

    @Nested
    @DisplayName("'Complete task' endpoint")
    class CompleteTask {

        @Test
        @DisplayName("should complete task successfully")
        void shouldCompleteTaskSuccessfully() {
            // given
            String taskId = "00000000f6b5a229daa5525d";
            TaskData completedTaskData = Sample.completedTaskData();
            given(taskCommandHandler.handle(new CompleteTaskCommand(taskId))).willReturn(completedTaskData);

            // when
            HttpResponse<TaskData> response = Interaction.completeTask(client, taskId);

            // then
            assertThat(response.getStatus().getCode()).isEqualTo(200);
            assertThat(response.getBody()).hasValueSatisfying(body -> {
                assertThat(body).usingRecursiveComparison().isEqualTo(completedTaskData);
            });
        }

        @Test
        @DisplayName("should return '400 bad request' when task ID is invalid")
        void shouldReturnBadRequest_whenTaskIdInvalid() {
            // given
            String taskId = "invalid-id";
            given(taskCommandHandler.handle(new CompleteTaskCommand(taskId))).willThrow(new InvalidTaskIdException("Invalid task ID format"));

            // when
            Exception exception = catchException(() -> Interaction.completeTask(client, taskId));

            // then
            assertThat(exception).isInstanceOfSatisfying(HttpClientResponseException.class, e -> {
                assertThat(e.getStatus().getCode()).isEqualTo(400);
                assertThat(e.getResponse().getBody(String.class)).hasValue("Invalid task ID format");
            });
        }

        @Test
        @DisplayName("should return '404 not found' when task not found")
        void shouldReturnNotFound_whenTaskNotFound() {
            // given
            String taskId = "00000000f6b5a229daa5525d";
            given(taskCommandHandler.handle(new CompleteTaskCommand(taskId))).willThrow(new TaskNotFoundException(TaskId.from(taskId)));

            // when
            Exception exception = catchException(() -> Interaction.completeTask(client, taskId));

            // then
            assertThat(exception).isInstanceOfSatisfying(HttpClientResponseException.class, e -> {
                assertThat(e.getStatus().getCode()).isEqualTo(404);
                assertThat(e.getResponse().getBody(String.class)).hasValue("Task with id 00000000f6b5a229daa5525d not found");
            });
        }

        @Test
        @DisplayName("should return '500 internal server error' when unexpected error occurs")
        void shouldReturnInternalServerError_whenUnexpectedError() {
            // given
            String taskId = "00000000f6b5a229daa5525d";
            given(taskCommandHandler.handle(new CompleteTaskCommand(taskId))).willThrow(new RuntimeException("Unexpected error"));

            // when
            Exception exception = catchException(() -> Interaction.completeTask(client, taskId));

            // then
            assertThat(exception).isInstanceOfSatisfying(HttpClientResponseException.class, e -> {
                assertThat(e.getStatus().getCode()).isEqualTo(500);
                assertThat(e.getResponse().getBody(String.class)).hasValue("Something went wrong");
            });
        }
    }

    @Nested
    @DisplayName("'Chat' endpoint")
    class Chat {

        @Test
        @DisplayName("should return response for prompt")
        void shouldReturnResponseForPrompt() {
            // given
            String message = "Hello";
            given(assistantQueryHandler.handle(new AskQuery(message))).willReturn("Hello, how can I help you?");

            // when
            HttpResponse<ChatResponse> response = Interaction.chat(client, message);

            // then
            assertThat(response.getStatus().getCode()).isEqualTo(200);
            assertThat(response.body()).satisfies((body) -> {
                assertThat(body.message()).isEqualTo("Hello, how can I help you?");
            });
        }

        @Test
        @DisplayName("should return '400 bad request' when prompt is empty")
        void shouldReturnBadRequest_whenPromptEmpty() {
            // given
            given(assistantQueryHandler.handle(any(AskQuery.class))).willThrow(new PromptIsEmptyException());

            // when
            Exception exception = catchException(() -> Interaction.chat(client, ""));

            // then
            assertThat(exception).isInstanceOfSatisfying(HttpClientResponseException.class, e -> {
                assertThat(e.getStatus().getCode()).isEqualTo(400);
                assertThat(e.getResponse().getBody(String.class)).hasValue("Prompt cannot be empty");
            });
        }

        @Test
        @DisplayName("should return '500 internal server error' when unexpected error occurs")
        void shouldReturnInternalServerError_whenUnexpectedError() {
            // given
            String message = "Hello";
            given(assistantQueryHandler.handle(new AskQuery(message))).willThrow(new RuntimeException("Unexpected error"));

            // when
            Exception exception = catchException(() -> Interaction.chat(client, message));

            // then
            assertThat(exception).isInstanceOfSatisfying(HttpClientResponseException.class, e -> {
                assertThat(e.getStatus().getCode()).isEqualTo(500);
                assertThat(e.getResponse().getBody(String.class)).hasValue("Something went wrong");
            });
        }

    }

    static class Interaction {

        static HttpResponse<GetAllTasksResponse> getTasks(HttpClient client) {
            return client.toBlocking().exchange(HttpRequest.GET("/tasks"), GetAllTasksResponse.class);
        }

        @SuppressWarnings("unchecked")
        static HttpResponse<TaskData> createNewTask(HttpClient client, CreateTaskRequest request) {
            return client.toBlocking().exchange(HttpRequest.POST("/tasks", request)
                    .contentType(MediaType.APPLICATION_JSON), TaskData.class);
        }

        @SuppressWarnings("unchecked")
        static HttpResponse<TaskData> findTaskById(HttpClient client, String id) {
            return client.toBlocking().exchange(HttpRequest.GET("/tasks/" + id), TaskData.class);
        }

        static HttpResponse<ChatResponse> chat(HttpClient client, String message) {
            return client.toBlocking().exchange(HttpRequest.POST("/chat", new ChatRequest(message))
                    .contentType(MediaType.APPLICATION_JSON), ChatResponse.class);
        }

        public static HttpResponse<TaskData> updateTask(HttpClient client, String taskId, UpdateTaskRequest updateTaskRequest) {
            return client.toBlocking().exchange(HttpRequest.PUT("/tasks/" + taskId, updateTaskRequest)
                    .contentType(MediaType.APPLICATION_JSON), TaskData.class);
        }

        public static HttpResponse<TaskData> completeTask(HttpClient client, String taskId) {
            return client.toBlocking().exchange(HttpRequest.POST("/tasks/" + taskId + "/complete", null)
                    .contentType(MediaType.APPLICATION_JSON), TaskData.class);
        }
    }

    static class Sample {

        static TaskData taskData() {
            return new TaskData("00000000f6b5a229daa5525d", "Task 1", "Task 1 Description", false);
        }

        static TaskData completedTaskData() {
            return new TaskData("00000000f6b5a229daa5525d", "Task 1", "Task 1 Description", true);
        }
    }

    @MockBean(TaskQueryHandler.class)
    TaskQueryHandler taskQueryHandler() {
        return Mockito.mock(TaskQueryHandler.class);
    }

    @MockBean(TaskCommandHandler.class)
    TaskCommandHandler taskCommandHandler() {
        return Mockito.mock(TaskCommandHandler.class);
    }

    @MockBean(AssistantQueryHandler.class)
    AssistantQueryHandler assistantQueryHandler() {
        return Mockito.mock(AssistantQueryHandler.class);
    }
}