package com.taskmanager.application.port.inbound.task.impl;

import com.taskmanager.application.Sample;
import com.taskmanager.application.domain.task.Task;
import com.taskmanager.application.domain.task.exception.InvalidTaskDescriptionException;
import com.taskmanager.application.domain.task.exception.InvalidTaskIdException;
import com.taskmanager.application.domain.task.exception.InvalidTaskTitleException;
import com.taskmanager.application.domain.task.exception.TaskNotFoundException;
import com.taskmanager.application.domain.task.valueobject.TaskId;
import com.taskmanager.application.port.inbound.task.TaskCommandHandler;
import com.taskmanager.application.port.inbound.task.command.CompleteTaskCommand;
import com.taskmanager.application.port.inbound.task.command.CreateTaskCommand;
import com.taskmanager.application.port.inbound.task.command.UpdateTaskCommand;
import com.taskmanager.application.port.inbound.task.data.TaskData;
import com.taskmanager.application.port.outbound.task.TaskCommandRepository;
import com.taskmanager.application.port.outbound.task.TaskCommandRepositoryMock;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;
import static org.junit.jupiter.api.Assertions.assertAll;

@MicronautTest(startApplication = false)
@DisplayName("Task Command Use Cases")
class TaskCommandHandlerImplTest {

    @Inject
    TaskCommandHandler taskCommandHandler;

    @Inject
    TaskCommandRepository taskCommandRepository;

    @Nested
    @DisplayName("Use case: Create task")
    class CreateTaskUseCase {

        @Test
        @DisplayName("should create new task")
        void shouldCreateNewTask() {
            // given
            String taskTitle = "Task title";
            String taskDescription = "I need to do something";

            // when
            TaskData actualResult = taskCommandHandler.handle(new CreateTaskCommand(taskTitle, taskDescription));

            // then
            assertAll("The created task should be returned back", () -> {
                assertThat(actualResult.id()).isNotNull();
                assertThat(actualResult.title()).isEqualTo(taskTitle);
                assertThat(actualResult.description()).isEqualTo(taskDescription);
            });

            assertAll("The created task should be saved in the repo", () -> {
                Optional<Task> savedTask = taskCommandRepository.findById(TaskId.from(actualResult.id()));
                assertThat(savedTask).hasValueSatisfying(task -> {
                    assertThat(task.getId()).isNotNull();
                    assertThat(task.getTitle().value()).isEqualTo(taskTitle);
                    assertThat(task.getDescription().value()).isEqualTo(taskDescription);
                });
            });
        }

        @Test
        @DisplayName("should create new task when using max length for title")
        void shouldCreateNewTask_whenUsingMaxValueForTitle() {
            // given
            String taskTitle = "a".repeat(100);
            String taskDescription = "I need to do something";

            // when
            TaskData actualResult = taskCommandHandler.handle(new CreateTaskCommand(taskTitle, taskDescription));

            // then
            assertAll("The created task should be returned back", () -> {
                assertThat(actualResult.title()).isEqualTo(taskTitle);
            });

            assertAll("The created task should be saved in the repo", () -> {
                Optional<Task> savedTask = taskCommandRepository.findById(TaskId.from(actualResult.id()));
                assertThat(savedTask).hasValueSatisfying(task -> {
                    assertThat(task.getTitle().value()).isEqualTo(taskTitle);
                });
            });
        }

        @Test
        @DisplayName("should create new task when using max length for description")
        void shouldCreateNewTask_whenUsingMaxValueForDescription() {
            // given
            String taskTitle = "Task title";
            String taskDescription = "b".repeat(1000);

            // when
            TaskData actualResult = taskCommandHandler.handle(new CreateTaskCommand(taskTitle, taskDescription));

            // then
            assertAll("The created task should be returned back", () -> {
                assertThat(actualResult.description()).isEqualTo(taskDescription);
            });

            assertAll("The created task should be saved in the repo", () -> {
                Optional<Task> savedTask = taskCommandRepository.findById(TaskId.from(actualResult.id()));
                assertThat(savedTask).hasValueSatisfying(task -> {
                    assertThat(task.getDescription().value()).isEqualTo(taskDescription);
                });
            });
        }

        @Test
        @DisplayName("should throw exception when title is too long")
        void shouldThrowException_whenTitleTooLong() {
            // given
            String taskTitle = "a".repeat(101);
            String taskDescription = "I need to do something";

            // when
            Exception exception = catchException(() -> taskCommandHandler.handle(new CreateTaskCommand(taskTitle, taskDescription)));

            // then
            assertThat(exception)
                    .isInstanceOf(InvalidTaskTitleException.class)
                    .hasMessage("Title cannot be longer than 100 characters");
        }

        @Test
        @DisplayName("should throw exception when description is too long")
        void shouldThrowException_whenDescriptionTooLong() {
            // given
            String taskTitle = "Task title";
            String taskDescription = "a".repeat(1001);

            // when
            Exception exception = catchException(() -> taskCommandHandler.handle(new CreateTaskCommand(taskTitle, taskDescription)));

            // then
            assertThat(exception)
                    .isInstanceOf(InvalidTaskDescriptionException.class)
                    .hasMessage("Description cannot be longer than 1000 characters");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("should throw exception when title is empty")
        void shouldThrowException_whenTitleIsEmpty(String taskTitle) {
            // given
            String taskDescription = "I need to do something";

            // when
            Exception exception = catchException(() -> taskCommandHandler.handle(new CreateTaskCommand(taskTitle, taskDescription)));

            // then
            assertThat(exception)
                    .isInstanceOf(InvalidTaskTitleException.class)
                    .hasMessage("Title cannot be empty");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("should create task when description is empty")
        void shouldCreateTask_whenEmptyDescription(String taskDescription) {
            // given
            String taskTitle = "Task title";

            // when
            TaskData actualResult = taskCommandHandler.handle(new CreateTaskCommand(taskTitle, taskDescription));

            // then
            assertAll("The created task should be returned back", () -> {
                assertThat(actualResult.id()).isNotNull();
                assertThat(actualResult.title()).isEqualTo(taskTitle);
                assertThat(actualResult.description()).isNullOrEmpty();
            });

            assertAll("The created task should be saved in the repo", () -> {
                Optional<Task> savedTask = taskCommandRepository.findById(TaskId.from(actualResult.id()));
                assertThat(savedTask).hasValueSatisfying(task -> {
                    assertThat(task.getTitle().value()).isEqualTo(taskTitle);
                    assertThat(task.getDescription().value()).isNullOrEmpty();
                });
            });
        }
    }

    @Nested
    @DisplayName("Use case: Complete task")
    class CompleteTaskUseCase {

        @Test
        @DisplayName("should complete existing task")
        void shouldCompleteExistingTask() {
            // given
            Task task = taskCommandRepository.save(Sample.task());
            assertThat(task.isCompleted()).isFalse();

            String taskId = Objects.requireNonNull(task.getId()).value();

            // when
            taskCommandHandler.handle(new CompleteTaskCommand(taskId));

            // then
            Optional<Task> taskFromRepo = taskCommandRepository.findById(TaskId.from(taskId));
            assertThat(taskFromRepo).hasValueSatisfying(t -> {
                assertThat(t.isCompleted()).isTrue();
            });
        }

        @Test
        @DisplayName("should throw exception when task not found")
        void shouldThrowException_whenTaskNotFound() {
            // given
            String nonExistentTaskId = "000000000000000000000000";

            // when
            Exception exception = catchException(() -> taskCommandHandler.handle(new CompleteTaskCommand(nonExistentTaskId)));

            // then
            assertThat(exception)
                    .isInstanceOf(TaskNotFoundException.class)
                    .hasMessageContaining(nonExistentTaskId);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("should throw exception when task ID is empty")
        void shouldThrowException_whenTaskIdIsEmpty(String emptyTaskId) {
            // when
            Exception exception = catchException(() -> taskCommandHandler.handle(new CompleteTaskCommand(emptyTaskId)));

            // then
            assertThat(exception)
                    .isInstanceOf(InvalidTaskIdException.class)
                    .hasMessage("Task ID cannot be empty");
        }
    }

    @Nested
    @DisplayName("Use case: Update task")
    class UpdateTaskUseCase {

        @Test
        @DisplayName("should update existing task")
        void shouldUpdateExistingTask() {
            // given
            Task task = taskCommandRepository.save(Sample.task());

            String taskId = Objects.requireNonNull(task.getId()).value();
            String newTitle = "Updated title";
            String newDescription = "Updated description";

            // when
            TaskData actualResult = taskCommandHandler.handle(new UpdateTaskCommand(taskId, newTitle, newDescription));

            // then
            assertAll("The updated task should be returned back", () -> {
                assertThat(actualResult.id()).isEqualTo(taskId);
                assertThat(actualResult.title()).isEqualTo(newTitle);
                assertThat(actualResult.description()).isEqualTo(newDescription);
            });

            assertAll("The task should be updated in the repo", () -> {
                Optional<Task> updatedTask = taskCommandRepository.findById(TaskId.from(taskId));
                assertThat(updatedTask).hasValueSatisfying(t -> {
                    assertThat(t.getTitle().value()).isEqualTo(newTitle);
                    assertThat(t.getDescription().value()).isEqualTo(newDescription);
                });
            });
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("should update description to empty")
        void shouldUpdateDescriptionToEmpty(String emptyTaskDescription) {
            // given
            Task task = taskCommandRepository.save(Sample.task());
            String taskId = Objects.requireNonNull(task.getId()).value();

            // when
            TaskData actualResult = taskCommandHandler.handle(new UpdateTaskCommand(taskId, "Task Title", emptyTaskDescription));

            // then
            assertAll("The updated task should be returned back", () -> {
                assertThat(actualResult.id()).isEqualTo(taskId);
                assertThat(actualResult.description()).isNullOrEmpty();
            });

            assertAll("The task should be updated in the repo", () -> {
                Optional<Task> updatedTask = taskCommandRepository.findById(TaskId.from(taskId));
                assertThat(updatedTask).hasValueSatisfying(t -> {
                    assertThat(t.getDescription().value()).isNullOrEmpty();
                });
            });
        }

        @Test
        @DisplayName("should update description to maximum length")
        void shouldUpdateDescriptionToMaximumLength() {
            // given
            Task task = taskCommandRepository.save(Sample.task());
            String taskId = Objects.requireNonNull(task.getId()).value();
            String maxLengthDescription = "a".repeat(1000);

            // when
            TaskData actualResult = taskCommandHandler.handle(new UpdateTaskCommand(taskId, "Task Title", maxLengthDescription));

            // then
            assertAll("The updated task should have the maximum length description", () -> {
                assertThat(actualResult.description()).isEqualTo(maxLengthDescription);
            });
        }

        @Test
        @DisplayName("should throw exception when description is too long")
        void shouldThrowException_whenDescriptionTooLong() {
            // given
            Task task = taskCommandRepository.save(Sample.task());
            String taskId = Objects.requireNonNull(task.getId()).value();
            String tooLongDescription = "a".repeat(1001);

            // when
            Exception exception = catchException(() -> taskCommandHandler.handle(new UpdateTaskCommand(taskId, "Task Title", tooLongDescription)));

            // then
            assertThat(exception)
                    .isInstanceOf(InvalidTaskDescriptionException.class)
                    .hasMessage("Description cannot be longer than 1000 characters");
        }

        @Test
        @DisplayName("should throw exception when task not found")
        void shouldThrowException_whenTaskNotFound() {
            // given
            String nonExistentTaskId = "000000000000000000000000";

            // when
            Exception exception = catchException(() -> taskCommandHandler.handle(new UpdateTaskCommand(nonExistentTaskId, "any", "andy")));

            // then
            assertThat(exception)
                    .isInstanceOf(TaskNotFoundException.class)
                    .hasMessageContaining(nonExistentTaskId);
        }
    }

    @MockBean(TaskCommandRepository.class)
    TaskCommandRepository taskCommandRepository() {
        return new TaskCommandRepositoryMock();
    }

}