package org.taskflow.wrapper;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.taskflow.model.Group;

import java.util.List;

public class UserGroupRequest {

    private Group group;
    private List<String> emails;

    public UserGroupRequest(Group group, List<String> emails) {
        this.group = group;
        this.emails = emails;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public List<String> getEmails() {
        return emails;
    }

    public void setEmails(List<String> emails) {
        this.emails = emails;
    }
}
