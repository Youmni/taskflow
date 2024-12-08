package org.taskflow.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
public class TaskGroup {

    @EmbeddedId
    TaskGroupKey id;

    @ManyToOne
    @MapsId("taskId")
    @JoinColumn(name = "task_id")
    @NotNull(message = "Task is required")
    @JsonBackReference
    private Task task;

    @ManyToOne
    @MapsId("groupId")
    @JoinColumn(name = "group_id")
    @NotNull(message = "Group is required")
    @JsonBackReference
    private Group group;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Permission is required. Accepted values are: READ, WRITE, DELETE.\n" +
            "Where:\n" +
            "READ allows read access.\n" +
            "WRITE includes read access and allows write access.\n" +
            "DELETE includes read and write access, and allows delete access.")
    @Column(name = "permission")
    private Permission permission;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    protected TaskGroup() {}

    public TaskGroup(Task task, Group group, Permission permission) {
        this.task = task;
        this.group = group;
        this.permission = permission;
    }

    public TaskGroupKey getId() {
        return id;
    }

    public void setId(TaskGroupKey id) {
        this.id = id;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public @NotNull(message = "Permission is required. Accepted values are: READ, WRITE, DELETE.\n" +
            "Where:\n" +
            "READ allows read access.\n" +
            "WRITE includes read access and allows write access.\n" +
            "DELETE includes read and write access, and allows delete access.") Permission getPermission() {
        return permission;
    }

    public void setPermission(@NotNull(message = "Permission is required. Accepted values are: READ, WRITE, DELETE.\n" +
            "Where:\n" +
            "READ allows read access.\n" +
            "WRITE includes read access and allows write access.\n" +
            "DELETE includes read and write access, and allows delete access.") Permission permission) {
        this.permission = permission;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

}
