package org.taskflow.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class TaskGroupKey implements Serializable {

    @Column(name = "task_id")
    private int taskId;

    @Column(name = "group_id")
    private int groupId;

    protected TaskGroupKey() {}

    public TaskGroupKey(int taskId, int groupId) {
        this.taskId = taskId;
        this.groupId = groupId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskGroupKey that = (TaskGroupKey) o;
        return taskId == that.taskId && groupId == that.groupId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId, groupId);
    }

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
