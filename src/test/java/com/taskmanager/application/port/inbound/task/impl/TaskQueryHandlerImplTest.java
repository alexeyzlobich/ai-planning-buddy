package com.taskmanager.application.port.inbound.task.impl;

import com.taskmanager.application.Sample;
import com.taskmanager.application.domain.task.Task;
import com.taskmanager.application.domain.task.exception.InvalidTaskIdException;
import com.taskmanager.application.domain.task.exception.TaskNotFoundException;
import com.taskmanager.application.port.inbound.task.TaskQueryHandler;
import com.taskmanager.application.port.inbound.task.data.TaskData;
import com.taskmanager.application.port.inbound.task.query.FindTaskByIdQuery;
import com.taskmanager.application.port.inbound.task.query.GetAvailableTasksQuery;
import com.taskmanager.application.port.outbound.task.TaskCommandRepository;
import com.taskmanager.application.port.outbound.task.TaskCommandRepositoryMock;
import com.taskmanager.application.port.outbound.task.TaskQueryRepository;
import com.taskmanager.application.port.outbound.task.TaskQueryRepositoryMock;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchException;

@MicronautTest(startApplication = false)
@DisplayName("Task Query Use Cases")
class TaskQueryHandlerImplTest {

    @Inject
    TaskQueryHandler taskQueryHandler;

    @Inject
    TaskCommandRepository taskCommandRepository;

    @Nested
    @DisplayName("Use case: Find task by id")
    class FindTaskByIdUseCase {

        @Test
        @SuppressWarnings("DataFlowIssue")
        @DisplayName("should find task by id")
        void shouldFindTaskById() {
            // given
            taskCommandRepository.save(Sample.task());
            Task taskToFind = taskCommandRepository.save(Sample.task());
            taskCommandRepository.save(Sample.task());

            // when
            TaskData actualResult = taskQueryHandler.handle(new FindTaskByIdQuery(taskToFind.getId().value()));

            // then
            assertThat(actualResult.id()).isEqualTo(taskToFind.getId().value());
            assertThat(actualResult.title()).isEqualTo(taskToFind.getTitle().value());
            assertThat(actualResult.description()).isEqualTo(taskToFind.getDescription().value());
        }

        @Test
        @DisplayName("should return empty when no task")
        void shouldReturnEmpty_whenNoTask() {
            // when
            Exception exception = catchException(() -> taskQueryHandler.handle(new FindTaskByIdQuery("00000000f6b5a229daa5525c")));

            // then
            assertThat(exception).isInstanceOf(TaskNotFoundException.class)
                    .hasMessage("Task with id 00000000f6b5a229daa5525c not found");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("should throw exception when id is empty")
        void shouldThrowException_whenIdIsEmpty(String id) {
            // when
            Exception exception = catchException(() -> taskQueryHandler.handle(new FindTaskByIdQuery(id)));

            // then
            assertThat(exception)
                    .isInstanceOf(InvalidTaskIdException.class)
                    .hasMessage("Task ID cannot be empty");
        }
    }

    @Nested
    @DisplayName("Use case: Get all tasks")
    class GetAvailableTasksUseCase {

        @Test
        @DisplayName("should return all tasks")
        void shouldReturnAllTasks() {
            // given
            taskCommandRepository.save(Sample.task("I need to do something"));
            taskCommandRepository.save(Sample.task("Or maybe not"));

            // when
            List<TaskData> actualResult = taskQueryHandler.handle(new GetAvailableTasksQuery());

            // then
            assertThat(actualResult).hasSize(2);
            assertThat(actualResult.get(0)).satisfies(taskData -> {
                assertThat(taskData.title()).isEqualTo("I need to do something");
            });
            assertThat(actualResult.get(1)).satisfies(taskData -> {
                assertThat(taskData.title()).isEqualTo("Or maybe not");
            });
        }

        @Test
        @DisplayName("should return empty list when no tasks")
        void shouldReturnEmptyList_whenNoTasks() {
            // when
            List<TaskData> actualResult = taskQueryHandler.handle(new GetAvailableTasksQuery());

            // then
            assertThat(actualResult).isEmpty();
        }
    }

    @MockBean(TaskQueryRepository.class)
    TaskQueryRepository taskQueryRepository() {
        return new TaskQueryRepositoryMock();
    }

    @MockBean(TaskCommandRepository.class)
    TaskCommandRepository taskCommandRepository() {
        return new TaskCommandRepositoryMock();
    }

}