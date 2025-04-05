package com.taskmanager.application.domain.task;

import com.taskmanager.application.domain.shared.DomainException;
import com.taskmanager.application.domain.shared.Entity;
import com.taskmanager.application.domain.task.state.TaskState;
import com.taskmanager.application.domain.task.valueobject.*;
import com.taskmanager.application.domain.user.valueobject.UserId;
import io.micronaut.core.util.CollectionUtils;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.*;

import static java.util.Optional.ofNullable;

public class Task implements Entity<TaskId> {

    private static final int MAX_TAGS_SIZE = 3;

    @Nullable // in a case if task is not yet saved
    private TaskId id;

    @Nonnull
    private final UserId userId;

    @Nonnull
    private TaskTitle title;

    @Nonnull
    private TaskDescription description;

    @Nonnull
    private TaskStatus status;

    private final Set<TagId> tagIds = new LinkedHashSet<>(MAX_TAGS_SIZE);

    public Task(@Nonnull TaskTitle title, @Nonnull UserId userId) {
        this.userId = userId;
        this.title = title;
        this.description = TaskDescription.from(null);
        this.status = TaskStatus.TODO;
    }

    public void markComplete() {
        this.status = TaskStatus.COMPLETED;
    }

    public boolean isCompleted() {
        return this.status == TaskStatus.COMPLETED;
    }

    public void addTags(@Nonnull TagId... tags) throws DomainException {
        if ((this.tagIds.size() + tags.length) > MAX_TAGS_SIZE) {
            throw new DomainException("Task cannot have more than " + MAX_TAGS_SIZE + " tags");
        }
        this.tagIds.addAll(Arrays.asList(tags));
    }

    public void removeTags(@Nonnull TagId... tags) {
        Arrays.asList(tags).forEach(this.tagIds::remove);
    }

    public @Nonnull List<TagId> getTags() {
        return List.copyOf(tagIds);
    }

    public @Nullable TaskId getId() {
        return id;
    }

    public @Nonnull TaskTitle getTitle() {
        return title;
    }

    public void setTitle(@Nonnull TaskTitle title) {
        this.title = title;
    }

    public @Nonnull TaskDescription getDescription() {
        return description;
    }

    public void setDescription(@Nonnull TaskDescription description) {
        this.description = description;
    }

    public @Nonnull UserId getUserId() {
        return userId;
    }

    // ------ State Conversion ------

    public @Nonnull TaskState toState() {
        return TaskState.builder()
                .id(ofNullable(id)
                        .map(TaskId::value)
                        .orElse(null))
                .userId(userId.value())
                .title(title.value())
                .description(description.value())
                .status(status.name())
                .tags(tagIds.stream()
                        .map(TagId::value)
                        .toList())
                .build();
    }

    public static @Nonnull Task fromState(@Nonnull TaskState state) {
        Task task = new Task(TaskTitle.from(state.title()), UserId.from(state.userId()));
        task.id = TaskId.from(Objects.requireNonNull(state.id()));
        task.description = TaskDescription.from(state.description());
        task.status = TaskStatus.valueOf(state.status());
        if (CollectionUtils.isNotEmpty(state.tags())) {
            task.tagIds.addAll(state.tags()
                    .stream()
                    .map(TagId::from)
                    .toList());
        }
        return task;
    }

    // ------ Equals and HashCode ------

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        if (id == null && task.id == null) { // handle case when unsaved tasks are compared
            return super.equals(o);
        } else {
            return Objects.equals(id, task.id);
        }
    }

    @Override
    public int hashCode() {
        if (id == null) { // handle case when unsaved tasks are compared
            return super.hashCode();
        } else {
            return Objects.hashCode(id);
        }
    }
}
