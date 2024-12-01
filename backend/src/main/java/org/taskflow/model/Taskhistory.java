package org.taskflow.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class Taskhistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private int historyId;

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
    private Priority priority;

    @NotBlank(message = "Due Date is required")
    @Column(name = "due_date")
    private LocalDate dueDate;

    @Size(max = 256, message = "Comment must be between 0 and 256 characters")
    @Column(name = "comment")
    private String comment;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "User is required")
    private User user;

    @ManyToOne
    @JoinColumn(name = "task_id", nullable = false)
    @NotNull(message = "Task is required")
    private Task task;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    protected Taskhistory() {}

    public Taskhistory(String title, String description, Status status, Priority priority, LocalDate dueDate, String comment, User user, Task task) {

        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.dueDate = dueDate;
        this.comment = comment;
        this.user = user;
        this.task = task;
    }

    public int getHistoryId() {
        return historyId;
    }

    public void setHistoryId(int historyId) {
        this.historyId = historyId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public @NotNull(message = "Task is required") Task getTask() {
        return task;
    }

    public void setTask(@NotNull(message = "Task is required") Task task) {
        this.task = task;
    }

    public @NotNull(message = "User is required") User getUser() {
        return user;
    }

    public void setUser(@NotNull(message = "User is required") User user) {
        this.user = user;
    }

    public @Size(min = 0, max = 256, message = "Comment must be between 0 and 256 characters") String getComment() {
        return comment;
    }

    public void setComment(@Size(min = 0, max = 256, message = "Comment must be between 0 and 256 characters") String comment) {
        this.comment = comment;
    }

    @NotNull(message = "Priority is required. Accepted values are: LOW, NORMAL, HIGH")
    public Priority getPriority() {
        return priority;
    }

    public void setPriority(@NotNull(message = "Priority is required. Accepted values are: LOW, NORMAL, HIGH") Priority priority) {
        this.priority = priority;
    }

    public @NotNull(message = "Status is required. Accepted values are: IN_PROGRESS, COMPLETED, CANCELLED, FAILED, VERIFIED, OVERDUE") Status getStatus() {
        return status;
    }

    public void setStatus(@NotNull(message = "Status is required. Accepted values are: IN_PROGRESS, COMPLETED, CANCELLED, FAILED, VERIFIED, OVERDUE") Status status) {
        this.status = status;
    }

    public @NotBlank(message = "Description is required") @Size(min = 25, max = 512, message = "Description must be between 25 and 512 characters") String getDescription() {
        return description;
    }

    public void setDescription(@NotBlank(message = "Description is required") @Size(min = 25, max = 512, message = "Description must be between 25 and 512 characters") String description) {
        this.description = description;
    }

    public @NotBlank(message = "Title is required") @Size(min = 5, max = 40, message = "Title must be between 5 and 40 characters") String getTitle() {
        return title;
    }

    public void setTitle(@NotBlank(message = "Title is required") @Size(min = 5, max = 40, message = "Title must be between 5 and 40 characters") String title) {
        this.title = title;
    }

    public @NotBlank(message = "Due Date is required") LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(@NotBlank(message = "Due Date is required") LocalDate dueDate) {
        this.dueDate = dueDate;
    }

}
