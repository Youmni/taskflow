package org.taskflow.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
    private int taskId;

    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 40, message = "Title must be between 5 and 40 characters")
    @Column(name = "title")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(min = 25, max = 512, message = "Description must be between 25 and 512 characters")
    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Status is required. Accepted values are: IN_PROGRESS, COMPLETED, CANCELLED, FAILED, VERIFIED, OVERDUE")
    @Column(name = "status")
    private Status status;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Priority is required. Accepted values are: LOW, NORMAL, HIGH")
    @Column(name = "priority")
    private int priority;

    @NotBlank(message = "Due Date is required")
    @Column(name = "due_date")
    private LocalDate dueDate;

    @Size(max = 256)
    @Column(name = "comment")
    private String comment;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "User is required")
    private User user;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    private Set<Taskhistory> taskhistories;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<TaskGroup> taskGroups;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected Task() {}

    public Task(String title, String description, Status status, int priority, LocalDate dueDate, String comment, User user) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.dueDate = dueDate;
        this.comment = comment;
        this.user = user;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public @NotBlank(message = "Title is required") @Size(min = 5, max = 40, message = "Title must be between 5 and 40 characters") String getTitle() {
        return title;
    }

    public void setTitle(@NotBlank(message = "Title is required") @Size(min = 5, max = 40, message = "Title must be between 5 and 40 characters") String title) {
        this.title = title;
    }

    public @NotBlank(message = "Description is required") @Size(min = 25, max = 512, message = "Description must be between 25 and 512 characters") String getDescription() {
        return description;
    }

    public void setDescription(@NotBlank(message = "Description is required") @Size(min = 25, max = 512, message = "Description must be between 25 and 512 characters") String description) {
        this.description = description;
    }

    public @NotNull(message = "Status is required. Accepted values are: IN_PROGRESS, COMPLETED, CANCELLED, FAILED, VERIFIED, OVERDUE") Status getStatus() {
        return status;
    }

    public void setStatus(@NotNull(message = "Status is required. Accepted values are: IN_PROGRESS, COMPLETED, CANCELLED, FAILED, VERIFIED, OVERDUE") Status status) {
        this.status = status;
    }

    @NotNull(message = "Priority is required. Accepted values are: LOW, NORMAL, HIGH")
    public int getPriority() {
        return priority;
    }

    public void setPriority(@NotNull(message = "Priority is required. Accepted values are: LOW, NORMAL, HIGH") int priority) {
        this.priority = priority;
    }

    public @NotBlank(message = "Due Date is required") LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(@NotBlank(message = "Due Date is required") LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public @Size(min = 0, max = 256) String getComment() {
        return comment;
    }

    public void setComment(@Size(min = 0, max = 256) String comment) {
        this.comment = comment;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Set<Taskhistory> getTaskhistories() {
        return taskhistories;
    }

    public void setTaskhistories(Set<Taskhistory> taskhistories) {
        this.taskhistories = taskhistories;
    }

    public Set<TaskGroup> getTaskGroups() {
        return taskGroups;
    }

    public void setTaskGroups(Set<TaskGroup> taskGroups) {
        this.taskGroups = taskGroups;
    }

}
