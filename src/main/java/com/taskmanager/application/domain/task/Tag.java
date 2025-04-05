package com.taskmanager.application.domain.task;

import com.taskmanager.application.domain.shared.Entity;
import com.taskmanager.application.domain.task.state.TagState;
import com.taskmanager.application.domain.task.valueobject.TagId;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Objects;

import static java.util.Optional.ofNullable;

public class Tag implements Entity<TagId> {

    @Nullable // in a case if tag is not yet saved
    private TagId id;

    @Nonnull
    private String name;

    @Nullable
    private String description;

    public Tag(@Nonnull String name) {
        this.name = name;
    }

    public @Nullable TagId getId() {
        return id;
    }

    public @Nonnull String getName() {
        return name;
    }

    public void setName(@Nonnull String name) {
        this.name = name;
    }

    public @Nullable String getDescription() {
        return description;
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    // ------ State Conversion ------

    public TagState toState() {
        return TagState.builder()
                .id(ofNullable(id)
                        .map(TagId::value)
                        .orElse(null))
                .name(name)
                .description(description)
                .build();
    }

    public static Tag fromState(@Nonnull TagState state) {
        Tag tag = new Tag(state.name());
        tag.id = TagId.from(Objects.requireNonNull(state.id()));
        tag.description = state.description();
        return tag;
    }

    // ------ Equals and HashCode ------

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Tag tag = (Tag) o;
        if (id == null && tag.id == null) { // in a case if tag is not yet saved
            return super.equals(o);
        } else {
            return Objects.equals(id, tag.id);
        }
    }

    @Override
    public int hashCode() {
        if (id == null) { // in a case if tag is not yet saved
            return super.hashCode();
        } else {
            return Objects.hashCode(id);
        }
    }
}
