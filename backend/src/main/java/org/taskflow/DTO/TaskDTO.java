package org.taskflow.DTO;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.taskflow.model.Permission;
import org.taskflow.model.Priority;
import org.taskflow.model.Status;
import org.taskflow.model.User;

import java.time.LocalDate;
import java.util.HashMap;

public class TaskDTO {

    private int taskId;
    private String title;
    private String description;
    private Status status;
    private Priority priority;
    private LocalDate dueDate;
    private String comment;
    private int userId;
    private HashMap<Integer, Permission> groups;

    public TaskDTO() {
    }

    public TaskDTO(int taskId, HashMap<Integer, Permission> groups, int userId, LocalDate dueDate, Priority priority, Status status, String description, String title, String comment) {
        this.taskId = taskId;
        this.groups = groups;
        this.userId = userId;
        this.dueDate = dueDate;
        this.priority = priority;
        this.status = status;
        this.description = description;
        this.title = title;
        this.comment = comment;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public HashMap<Integer, Permission> getGroups() {
        return groups;
    }

    public void setGroups(HashMap<Integer, Permission> groups) {
        this.groups = groups;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
