package org.taskflow.DTO;

import org.taskflow.enums.Priority;
import org.taskflow.enums.Status;

import java.time.LocalDate;

public class TaskRequest {

    private String title;
    private String description;
    private Status status;
    private Priority priority;
    private int day;
    private int month;
    private int year;
    private String comment;
    private int userId;


    public TaskRequest() {
    }

    public TaskRequest(String title, int userId, String comment, int year, int day, Priority priority, Status status, String description, int month) {
        this.title = title;
        this.userId = userId;
        this.comment = comment;
        this.year = year;
        this.day = day;
        this.priority = priority;
        this.status = status;
        this.description = description;
        this.month = month;
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

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
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
