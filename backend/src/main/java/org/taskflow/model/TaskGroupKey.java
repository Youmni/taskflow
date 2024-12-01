package org.taskflow.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class TaskGroupKey implements Serializable {

    @Column(name = "task_id")
    private int taskId;

    @Column(name = "group_id")
    private int groupId;

    protected TaskGroupKey() {}

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }
}
