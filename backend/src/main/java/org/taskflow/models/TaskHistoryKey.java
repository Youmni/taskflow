package org.taskflow.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class TaskHistoryKey implements Serializable {

    @Column(name = "task_id")
    private int taskId;

    @Column(name = "history_id")
    private int historyId;


    protected TaskHistoryKey() {}

    public TaskHistoryKey(int taskId, int historyId) {
        this.taskId = taskId;
        this.historyId = historyId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskHistoryKey that = (TaskHistoryKey) o;
        return historyId == that.historyId && taskId == that.taskId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(historyId, taskId);
    }

    public int getHistoryId() {
        return historyId;
    }

    public void setHistoryId(int historyId) {
        this.historyId = historyId;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }
}
