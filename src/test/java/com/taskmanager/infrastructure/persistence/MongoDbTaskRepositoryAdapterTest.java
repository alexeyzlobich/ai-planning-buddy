package com.taskmanager.infrastructure.persistence;

import com.taskmanager.application.Sample;
import com.taskmanager.application.domain.task.Task;
import com.taskmanager.application.domain.task.valueobject.TaskDescription;
import com.taskmanager.application.domain.task.valueobject.TaskId;
import com.taskmanager.application.domain.task.valueobject.TaskTitle;
import com.taskmanager.infrastructure.persistence.util.MongoDbExtension;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@ExtendWith(MongoDbExtension.class)
@MicronautTest(startApplication = false)
@DisplayName("Adapter: MongoDB Task Repository")
class MongoDbTaskRepositoryAdapterTest {

    @Inject
    MongoDbTaskRepositoryAdapter mongoDbTaskRepositoryAdapter;

    @Nested
    @DisplayName("Save task")
    class SaveTask {

        @Test
        @DisplayName("should save new task")
        void shouldSaveNewTask() {
            // given
            Task newTask = Sample.task();

            // when
            Task savedTask = mongoDbTaskRepositoryAdapter.save(newTask);

            // then
            assertAll("A new task is returned", () -> {
                assertThat(savedTask.getId()).isNotNull();
                assertThat(savedTask.getUserId()).isEqualTo(newTask.getUserId());
                assertThat(savedTask.getTitle()).isEqualTo(newTask.getTitle());
                assertThat(savedTask.getDescription()).isEqualTo(newTask.getDescription());
                assertThat(savedTask.isCompleted()).isEqualTo(newTask.isCompleted());
            });

            assertAll("The task is saved in the repository", () -> {
                Optional<Task> foundTask = mongoDbTaskRepositoryAdapter.findById(TaskId.from(savedTask.getId().value()));
                assertThat(foundTask).isPresent().hasValueSatisfying(task -> {
                    assertThat(task.getId()).isNotNull();
                    assertThat(task.getUserId()).isEqualTo(newTask.getUserId());
                    assertThat(task.getTitle()).isEqualTo(newTask.getTitle());
                    assertThat(task.getDescription()).isEqualTo(newTask.getDescription());
                    assertThat(task.isCompleted()).isEqualTo(newTask.isCompleted());
                });
            });

        }

        @Test
        @DisplayName("should update existing task")
        void shouldUpdateExistingTask() {
            // given
            Task taskToUpdate = mongoDbTaskRepositoryAdapter.save(Sample.task());

            taskToUpdate.setTitle(TaskTitle.from("Updated Title"));
            taskToUpdate.setDescription(TaskDescription.from("Updated Description"));
            taskToUpdate.markComplete();

            // when
            Task savedTask = mongoDbTaskRepositoryAdapter.save(taskToUpdate);

            // then
            assertAll("An updated task is returned", () -> {
                assertThat(savedTask.getId()).isNotNull();
                assertThat(savedTask.getUserId()).isEqualTo(taskToUpdate.getUserId());
                assertThat(savedTask.getTitle()).isEqualTo(taskToUpdate.getTitle());
                assertThat(savedTask.getDescription()).isEqualTo(taskToUpdate.getDescription());
                assertThat(savedTask.isCompleted()).isEqualTo(taskToUpdate.isCompleted());
            });

            assertAll("The task is saved in the repository", () -> {
                Optional<Task> foundTask = mongoDbTaskRepositoryAdapter.findById(TaskId.from(savedTask.getId().value()));
                assertThat(foundTask).isPresent().hasValueSatisfying(task -> {
                    assertThat(task.getId()).isNotNull();
                    assertThat(task.getUserId()).isEqualTo(taskToUpdate.getUserId());
                    assertThat(task.getTitle()).isEqualTo(taskToUpdate.getTitle());
                    assertThat(task.getDescription()).isEqualTo(taskToUpdate.getDescription());
                    assertThat(task.isCompleted()).isEqualTo(taskToUpdate.isCompleted());
                });
            });
        }
    }

    @Nested
    @DisplayName("Find task by ID")
    class FindTaskById {

        @Test
        @DisplayName("should return task when exists")
        void shouldReturnTask_whenExists() {
            // given
            Task savedTask = mongoDbTaskRepositoryAdapter.save(Sample.task());
            TaskId taskId = savedTask.getId();

            // when
            Optional<Task> foundTask = mongoDbTaskRepositoryAdapter.findById(taskId);

            // then
            assertThat(foundTask).isPresent().hasValueSatisfying(task -> {
                assertThat(task.getId()).isEqualTo(savedTask.getId());
                assertThat(task.getUserId()).isEqualTo(savedTask.getUserId());
                assertThat(task.getTitle()).isEqualTo(savedTask.getTitle());
                assertThat(task.getDescription()).isEqualTo(savedTask.getDescription());
                assertThat(task.isCompleted()).isEqualTo(savedTask.isCompleted());
            });
        }

        @Test
        @DisplayName("should return empty when task not found")
        void shouldReturnEmpty_whenTaskNotFound() {
            // given
            TaskId nonExistentId = TaskId.from("000000000000000000000000");

            // when
            Optional<Task> result = mongoDbTaskRepositoryAdapter.findById(nonExistentId);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Find all tasks")
    class FindAllTasks {

        @Test
        @DisplayName("should return all saved tasks")
        void shouldReturnAllSavedTasks() {
            // given
            mongoDbTaskRepositoryAdapter.save(Sample.task("Test Task 1"));
            mongoDbTaskRepositoryAdapter.save(Sample.task("Test Task 2"));

            // when
            List<Task> tasks = mongoDbTaskRepositoryAdapter.findAll();

            // then
            assertThat(tasks).hasSize(2);
            assertThat(tasks.get(0)).satisfies(task -> {
                assertThat(task.getId()).isNotNull();
                assertThat(task.getUserId().value()).isEqualTo("00000000f6b5a229daa5525d");
                assertThat(task.getTitle().value()).isEqualTo("Test Task 1");
                assertThat(task.getDescription().value()).isEqualTo("But I don't know what");
                assertThat(task.isCompleted()).isFalse();
            });
            assertThat(tasks.get(1)).satisfies(task -> {
                assertThat(task.getId()).isNotNull();
                assertThat(task.getUserId().value()).isEqualTo("00000000f6b5a229daa5525d");
                assertThat(task.getTitle().value()).isEqualTo("Test Task 2");
                assertThat(task.getDescription().value()).isEqualTo("But I don't know what");
                assertThat(task.isCompleted()).isFalse();
            });
        }

        @Test
        @DisplayName("should return empty list when no tasks")
        void shouldReturnEmptyList_whenNoTasks() {
            // when
            List<Task> tasks = mongoDbTaskRepositoryAdapter.findAll();

            // then
            assertThat(tasks).isEmpty();
        }
    }
}