package com.taskmanager.api.grpc;

import com.google.protobuf.Empty;
import com.taskmanager.api.grpc.generated.*;
import com.taskmanager.api.grpc.generated.TaskManagerControllerGrpc.TaskManagerControllerBlockingStub;
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
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@MicronautTest(startApplication = false)
@DisplayName("gRPC API specification: Task Manager")
public class TaskManagerGrpcControllerTest {

    @Inject
    TaskManagerControllerBlockingStub taskManagerGrpcController;

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
            TaskList response = taskManagerGrpcController.getTasks(Empty.getDefaultInstance());

            // then
            assertThat(response.getTasksList()).hasSize(1);
            assertThat(response.getTasks(0)).satisfies(task -> {
                assertThat(task.getId()).isEqualTo(taskData.id());
                assertThat(task.getTitle()).isEqualTo(taskData.title());
                assertThat(task.getDescription()).isEqualTo(taskData.description());
                assertThat(task.getCompleted()).isEqualTo(taskData.completed());
            });
        }

        @Test
        @DisplayName("should return empty list when no tasks")
        void shouldReturnEmptyList_whenNoTasks() {
            // given
            given(taskQueryHandler.handle(new GetAvailableTasksQuery())).willReturn(List.of());

            // when
            TaskList response = taskManagerGrpcController.getTasks(Empty.getDefaultInstance());

            // then
            assertThat(response.getTasksList()).isEmpty();
        }

        @Test
        @DisplayName("should handle internal error")
        void shouldHandleInternalError_whenGettingTasks() {
            // given
            given(taskQueryHandler.handle(new GetAvailableTasksQuery())).willThrow(new RuntimeException("Internal error"));

            // when
            Exception exception = catchException(() -> taskManagerGrpcController.getTasks(Empty.getDefaultInstance()));

            // then
            assertThat(exception).isInstanceOfSatisfying(StatusRuntimeException.class, e -> {
                assertThat(e.getStatus().getCode()).isEqualTo(Status.Code.INTERNAL);
                assertThat(e.getMessage()).contains("Something went wrong");
            });
        }
    }

    @Nested
    @DisplayName("'Create new task' endpoint")
    class CreateTask {

        @Test
        @DisplayName("should create task successfully")
        void shouldCreateTaskSuccessfully() {
            // given
            String title = "Test task";
            String description = "Task Description";
            TaskData createdTask = Sample.taskData();

            given(taskCommandHandler.handle(new CreateTaskCommand(title, description))).willReturn(createdTask);

            // when
            Task response = taskManagerGrpcController.createTask(CreateTaskRequest.newBuilder()
                    .setTitle(title)
                    .setDescription(description)
                    .build());

            // then
            // TODO: move asserts to a custom matcher to avoid duplication
            assertThat(response.getId()).isEqualTo(createdTask.id());
            assertThat(response.getTitle()).isEqualTo(createdTask.title());
            assertThat(response.getDescription()).isEqualTo(createdTask.description());
            assertThat(response.getCompleted()).isEqualTo(createdTask.completed());
        }

        @Test
        @DisplayName("should throw exception when title is invalid")
        void shouldThrowException_whenTitleInvalid() {
            // given
            given(taskCommandHandler.handle(new CreateTaskCommand("<invalid>", "Description")))
                    .willThrow(new InvalidTaskTitleException("Title is invalid"));

            // when/then
            assertThatThrownBy(() -> taskManagerGrpcController.createTask(CreateTaskRequest.newBuilder()
                    .setTitle("<invalid>")
                    .setDescription("Description")
                    .build()))
                    .isInstanceOf(StatusRuntimeException.class)
                    .hasMessageContaining("Title is invalid");
        }

        @Test
        @SuppressWarnings("ResultOfMethodCallIgnored")
        @DisplayName("should throw exception when description is invalid")
        void shouldThrowException_whenDescriptionInvalid() {
            // given
            given(taskCommandHandler.handle(new CreateTaskCommand("Title", "<invalid>")))
                    .willThrow(new InvalidTaskDescriptionException("Description is invalid"));

            // when
            Exception exception = catchException(() -> taskManagerGrpcController.createTask(CreateTaskRequest.newBuilder()
                    .setTitle("Title")
                    .setDescription("<invalid>")
                    .build()));

            // when/then
            assertThat(exception)
                    .isInstanceOf(StatusRuntimeException.class)
                    .hasMessageContaining("Description is invalid");
        }

        @Test
        @DisplayName("should handle internal error")
        void shouldHandleInternalError_whenCreatingTask() {
            // given
            given(taskCommandHandler.handle(any(CreateTaskCommand.class))).willThrow(new RuntimeException("Internal error"));

            // when
            Exception exception = catchException(() -> taskManagerGrpcController.createTask(CreateTaskRequest.newBuilder()
                    .setTitle("Title")
                    .setDescription("Description")
                    .build()));

            // then
            assertThat(exception).isInstanceOfSatisfying(StatusRuntimeException.class, e -> {
                assertThat(e.getStatus().getCode()).isEqualTo(Status.Code.INTERNAL);
                assertThat(e.getMessage()).contains("Something went wrong");
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
            Task response = taskManagerGrpcController.findTaskById(FindTaskByIdRequest.newBuilder()
                    .setId(taskId)
                    .build());

            // then
            assertThat(response.getId()).isEqualTo(taskData.id());
            assertThat(response.getTitle()).isEqualTo(taskData.title());
            assertThat(response.getDescription()).isEqualTo(taskData.description());
            assertThat(response.getCompleted()).isEqualTo(taskData.completed());
        }

        @Test
        @SuppressWarnings("ResultOfMethodCallIgnored")
        @DisplayName("should throw exception when task not found")
        void shouldThrowException_whenTaskNotFound() {
            // given
            String taskId = "00000000f6b5a229daa5525d";
            given(taskQueryHandler.handle(eq(new FindTaskByIdQuery(taskId)))).willThrow(new TaskNotFoundException(TaskId.from(taskId)));

            // when
            Exception exception = catchException(() -> taskManagerGrpcController.findTaskById(FindTaskByIdRequest.newBuilder()
                    .setId(taskId)
                    .build()));

            // then
            assertThat(exception)
                    .isInstanceOf(StatusRuntimeException.class)
                    .hasMessageContaining("NOT_FOUND");
        }

        @Test
        @SuppressWarnings("ResultOfMethodCallIgnored")
        @DisplayName("should return bad request when task ID invalid")
        void shouldReturnBadRequest_whenTaskIdInvalid() {
            // given
            String taskId = "<invalid>";
            given(taskQueryHandler.handle(eq(new FindTaskByIdQuery(taskId)))).willThrow(new InvalidTaskIdException("Invalid task ID"));

            // when
            Exception exception = catchException(() -> taskManagerGrpcController.findTaskById(FindTaskByIdRequest.newBuilder()
                    .setId(taskId)
                    .build()));

            // then
            assertThat(exception)
                    .isInstanceOf(StatusRuntimeException.class)
                    .hasMessageContaining("Invalid task ID");
        }

        @Test
        @DisplayName("should handle internal error")
        void shouldHandleInternalError_whenFindingTask() {
            // given
            given(taskQueryHandler.handle(any(FindTaskByIdQuery.class))).willThrow(new RuntimeException("Internal error"));

            // when
            Exception exception = catchException(() -> taskManagerGrpcController.findTaskById(FindTaskByIdRequest.newBuilder()
                    .setId("00000000f6b5a229daa5525d")
                    .build()));
            // then
            assertThat(exception).isInstanceOfSatisfying(StatusRuntimeException.class, e -> {
                assertThat(e.getStatus().getCode()).isEqualTo(Status.Code.INTERNAL);
                assertThat(e.getMessage()).contains("Something went wrong");
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
            String title = "Updated Title";
            String description = "Updated Description";

            TaskData updatedTask = Sample.taskData();
            given(taskCommandHandler.handle(new UpdateTaskCommand(taskId, title, description))).willReturn(updatedTask);

            // when
            Task response = taskManagerGrpcController.updateTask(UpdateTaskRequest.newBuilder()
                    .setId(taskId)
                    .setTitle(title)
                    .setDescription(description)
                    .build());

            // then
            assertThat(response.getId()).isEqualTo(updatedTask.id());
            assertThat(response.getTitle()).isEqualTo(updatedTask.title());
            assertThat(response.getDescription()).isEqualTo(updatedTask.description());
            assertThat(response.getCompleted()).isEqualTo(updatedTask.completed());
        }

        @Test
        @DisplayName("should throw exception when task not found")
        void shouldThrowException_whenTaskNotFound() {
            // given
            String taskId = "00000000f6b5a229daa5525d";
            given(taskCommandHandler.handle(any(UpdateTaskCommand.class))).willThrow(new TaskNotFoundException(TaskId.from(taskId)));

            // when
            Exception exception = catchException(() -> taskManagerGrpcController.updateTask(UpdateTaskRequest.newBuilder()
                    .setId(taskId)
                    .setTitle("any")
                    .build()));

            // then
            assertThat(exception).isInstanceOfSatisfying(StatusRuntimeException.class, e -> {
                assertThat(e.getStatus().getCode()).isEqualTo(Status.Code.NOT_FOUND);
            });
        }

        @Test
        @DisplayName("should throw exception when task ID is invalid")
        void shouldThrowException_whenTaskIdInvalid() {
            // given
            String taskId = "<invalid>";
            given(taskCommandHandler.handle(any(UpdateTaskCommand.class))).willThrow(new InvalidTaskIdException("Invalid task ID"));

            // when
            Exception exception = catchException(() -> taskManagerGrpcController.updateTask(UpdateTaskRequest.newBuilder()
                    .setId(taskId)
                    .setTitle("any")
                    .build()));

            // then
            assertThat(exception).isInstanceOfSatisfying(StatusRuntimeException.class, e -> {
                assertThat(e.getStatus().getCode()).isEqualTo(Status.Code.INVALID_ARGUMENT);
                assertThat(e.getMessage()).contains("Invalid task ID");
            });
        }

        @Test
        @DisplayName("should throw exception when title is invalid")
        void shouldThrowException_whenTitleInvalid() {
            // given
            String taskId = "00000000f6b5a229daa5525d";
            given(taskCommandHandler.handle(any(UpdateTaskCommand.class))).willThrow(new InvalidTaskTitleException("Title is invalid"));

            // when
            Exception exception = catchException(() -> taskManagerGrpcController.updateTask(UpdateTaskRequest.newBuilder()
                    .setId(taskId)
                    .setTitle("<invalid>")
                    .build()));

            // then
            assertThat(exception).isInstanceOfSatisfying(StatusRuntimeException.class, e -> {
                assertThat(e.getStatus().getCode()).isEqualTo(Status.Code.INVALID_ARGUMENT);
                assertThat(e.getMessage()).contains("Title is invalid");
            });
        }

        @Test
        @DisplayName("should handle internal error")
        void shouldHandleInternalError_whenUpdatingTask() {
            // given
            given(taskCommandHandler.handle(any(UpdateTaskCommand.class))).willThrow(new RuntimeException("Internal error"));

            // when
            Exception exception = catchException(() -> taskManagerGrpcController.updateTask(UpdateTaskRequest.newBuilder()
                    .setId("00000000f6b5a229daa5525d")
                    .setTitle("Title")
                    .build()));

            // then
            assertThat(exception).isInstanceOfSatisfying(StatusRuntimeException.class, e -> {
                assertThat(e.getStatus().getCode()).isEqualTo(Status.Code.INTERNAL);
                assertThat(e.getMessage()).contains("Something went wrong");
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
            TaskData completedTask = Sample.completedTaskData();

            given(taskCommandHandler.handle(new CompleteTaskCommand(taskId))).willReturn(completedTask);

            // when
            Task response = taskManagerGrpcController.completeTask(CompleteTaskRequest.newBuilder()
                    .setId(taskId)
                    .build());

            // then
            assertThat(response.getId()).isEqualTo(completedTask.id());
            assertThat(response.getTitle()).isEqualTo(completedTask.title());
            assertThat(response.getDescription()).isEqualTo(completedTask.description());
            assertThat(response.getCompleted()).isEqualTo(completedTask.completed());
        }

        @Test
        @DisplayName("should throw exception when task not found")
        void shouldThrowException_whenTaskNotFound() {
            // given
            String taskId = "00000000f6b5a229daa5525d";
            given(taskCommandHandler.handle(new CompleteTaskCommand(taskId))).willThrow(new TaskNotFoundException(TaskId.from(taskId)));

            // when
            Exception exception = catchException(() -> taskManagerGrpcController.completeTask(CompleteTaskRequest.newBuilder().setId(taskId).build()));

            // then
            assertThat(exception).isInstanceOfSatisfying(StatusRuntimeException.class, e -> {
                assertThat(e.getStatus().getCode()).isEqualTo(Status.Code.NOT_FOUND);
            });
        }

        @Test
        @DisplayName("should throw exception when task ID is invalid")
        void shouldThrowException_whenTaskIdInvalid() {
            // given
            String taskId = "<invalid>";
            given(taskCommandHandler.handle(new CompleteTaskCommand(taskId))).willThrow(new InvalidTaskIdException("Invalid task ID"));

            // when
            Exception exception = catchException(() -> taskManagerGrpcController.completeTask(CompleteTaskRequest.newBuilder().setId(taskId).build()));

            // then
            assertThat(exception).isInstanceOfSatisfying(StatusRuntimeException.class, e -> {
                assertThat(e.getStatus().getCode()).isEqualTo(Status.Code.INVALID_ARGUMENT);
                assertThat(e.getMessage()).contains("Invalid task ID");
            });
        }

        @Test
        @DisplayName("should handle internal error")
        void shouldHandleInternalError_whenCompletingTask() {
            // given
            given(taskCommandHandler.handle(any(CompleteTaskCommand.class))).willThrow(new RuntimeException("Internal error"));

            // when
            Exception exception = catchException(() -> taskManagerGrpcController.completeTask(CompleteTaskRequest.newBuilder()
                    .setId("00000000f6b5a229daa5525d")
                    .build()));

            // then
            assertThat(exception).isInstanceOfSatisfying(StatusRuntimeException.class, e -> {
                assertThat(e.getStatus().getCode()).isEqualTo(Status.Code.INTERNAL);
                assertThat(e.getMessage()).contains("Something went wrong");
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
            ChatResponse response = taskManagerGrpcController.chat(ChatRequest.newBuilder()
                    .setPrompt(message)
                    .build());

            // then
            assertThat(response.getResponse()).isEqualTo("Hello, how can I help you?");
        }

        @Test
        @DisplayName("should handle empty prompt")
        void shouldHandleEmptyPrompt() {
            // given
            given(assistantQueryHandler.handle(any(AskQuery.class))).willThrow(new PromptIsEmptyException());

            // when
            Exception exception = catchException(() -> taskManagerGrpcController.chat(ChatRequest.newBuilder()
                    .setPrompt("")
                    .build()));

            // then
            assertThat(exception).isInstanceOfSatisfying(StatusRuntimeException.class, e -> {
                assertThat(e.getStatus().getCode()).isEqualTo(Status.Code.INVALID_ARGUMENT);
                assertThat(e.getMessage()).contains("Prompt cannot be empty");
            });
        }

        @Test
        @DisplayName("should handle internal error")
        void shouldHandleInternalError() {
            // given
            given(assistantQueryHandler.handle(any(AskQuery.class))).willThrow(new RuntimeException("Internal error"));

            // when
            Exception exception = catchException(() -> taskManagerGrpcController.chat(ChatRequest.newBuilder()
                    .setPrompt("Something here")
                    .build()));

            // then
            assertThat(exception).isInstanceOfSatisfying(StatusRuntimeException.class, e -> {
                assertThat(e.getStatus().getCode()).isEqualTo(Status.Code.INTERNAL);
                assertThat(e.getMessage()).contains("Something went wrong");
            });
        }
    }

    static class Sample {
        static TaskData taskData() {
            return new TaskData("00000000f6b5a229daa5525d", "Task 1", "Task 1 Description", false);
        }

        static TaskData completedTaskData() {
            return new TaskData("00000000f6b5a229daa5525d", "Task 1", "Task 1 Description", false);
        }
    }

    @MockBean(TaskQueryHandler.class)
    TaskQueryHandler taskQueryHandler() {
        return mock(TaskQueryHandler.class);
    }

    @MockBean(TaskCommandHandler.class)
    TaskCommandHandler taskCommandHandler() {
        return mock(TaskCommandHandler.class);
    }

    @MockBean(AssistantQueryHandler.class)
    AssistantQueryHandler findTaskByIdUseCase() {
        return mock(AssistantQueryHandler.class);
    }

}