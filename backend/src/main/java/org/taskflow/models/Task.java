package org.taskflow.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.taskflow.enums.Priority;
import org.taskflow.enums.Status;

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
    private Priority priority;

    @NotNull(message = "Due Date is required")
    @Future(message = "Due Date must be in the future")
    @Column(name = "due_date")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate dueDate;

    @Size(max = 256)
    @Column(name = "comment")
    private String comment;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "User is required")
    private User user;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    @JsonBackReference
    private Set<Taskhistory> taskhistories;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonBackReference
    Set<TaskGroup> taskGroups;

    @CreationTimestamp
    @Column(name = "created_at")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime updatedAt;

    protected Task() {}

    public Task(String title, String description, Status status, Priority priority, LocalDate dueDate, String comment, User user) {
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
    public Priority getPriority() {
        return priority;
    }

    public void setPriority(@NotNull(message = "Priority is required. Accepted values are: LOW, NORMAL, HIGH") Priority priority) {
        this.priority = priority;
    }

    public @NotNull(message = "Due Date is required") LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(@NotNull(message = "Due Date is required") LocalDate dueDate) {
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
