package org.taskflow.wrapper;

import org.taskflow.model.Group;
import org.taskflow.model.Permission;
import org.taskflow.model.Task;

import java.util.HashMap;

public class TaskCreationRequest {

    private TaskRequest taskRequest;
    private HashMap<Integer, Permission> group;

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
