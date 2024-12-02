package org.taskflow.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class UserGroupKey implements Serializable {

    @Column(name = "user_id")
    private int userId;

    @Column(name = "group_id")
    private int groupId;

    protected UserGroupKey() {}

    public UserGroupKey(int userId, int groupId) {
        this.userId = userId;
        this.groupId = groupId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserGroupKey that = (UserGroupKey) o;
        return userId == that.userId && groupId == that.groupId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, groupId);
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }
}
