package org.taskflow.model;

import org.taskflow.enums.Permission;
import org.taskflow.enums.Priority;
import org.taskflow.enums.Status;

import java.time.LocalDate;
import java.util.HashMap;

public class TaskHistory {
    private int historyId;
    private String title;
    private String description;
    private Status status;
    private Priority priority;
    private LocalDate dueDate;
    private String comment;
    private int userId;
    private int taskId;
    private HashMap<Integer, Permission> groups;

    public TaskHistory() {
    }

    public TaskHistory(int historyId, String title, String description, Status status, Priority priority, String comment, LocalDate dueDate, int userId, HashMap<Integer, Permission> groups, int taskId) {
        this.historyId = historyId;
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.comment = comment;
        this.dueDate = dueDate;
        this.userId = userId;
        this.groups = groups;
        this.taskId = taskId;
    }

    public int getHistoryId() {
        return historyId;
    }

    public void setHistoryId(int historyId) {
        this.historyId = historyId;
    }

    public HashMap<Integer, Permission> getGroups() {
        return groups;
    }

    public void setGroups(HashMap<Integer, Permission> groups) {
        this.groups = groups;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
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
