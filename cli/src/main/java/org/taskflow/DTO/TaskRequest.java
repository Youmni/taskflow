package org.taskflow.DTO;

import org.taskflow.enums.Priority;
import org.taskflow.enums.Status;

import java.time.LocalDate;

public class TaskRequest {

    private String title;
    private String description;
    private Status status;
    private Priority priority;
    private LocalDate date;
    private String comment;
    private int userId;


    public TaskRequest() {
    }

    public TaskRequest(String description, String title, Status status, Priority priority, LocalDate date, String comment, int userId) {
        this.description = description;
        this.title = title;
        this.status = status;
        this.priority = priority;
        this.date = date;
        this.comment = comment;
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
