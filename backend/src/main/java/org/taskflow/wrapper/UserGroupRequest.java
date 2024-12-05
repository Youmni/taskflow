package org.taskflow.wrapper;

import org.taskflow.model.Group;

import java.util.List;

public class UserGroupRequest {

    private Group group;
    private List<Integer> users;

    public UserGroupRequest(Group group, List<Integer> users) {
        this.group = group;
        this.users = users;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public List<Integer> getUsers() {
        return users;
    }

    public void setUsers(List<Integer> users) {
        this.users = users;
    }
}
