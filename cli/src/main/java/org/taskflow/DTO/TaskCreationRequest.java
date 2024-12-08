package org.taskflow.DTO;

import org.taskflow.enums.Permission;

import java.util.HashMap;

public class TaskCreationRequest {

    private TaskRequest taskRequest;
    private HashMap<Integer, Permission> group;

    public TaskCreationRequest() {
    }

    public TaskCreationRequest(TaskRequest taskRequest, HashMap<Integer, Permission> group) {
        this.taskRequest = taskRequest;
        this.group = group;
    }

    public HashMap<Integer, Permission> getGroup() {
        return group;
    }

    public void setGroup(HashMap<Integer, Permission> group) {
        this.group = group;
    }

    public TaskRequest getTaskRequest() {
        return taskRequest;
    }

    public void setTaskRequest(TaskRequest taskRequest) {
        this.taskRequest = taskRequest;
    }

}
