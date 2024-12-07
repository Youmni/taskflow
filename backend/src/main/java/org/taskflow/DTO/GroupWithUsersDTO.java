package org.taskflow.DTO;

import java.util.List;

public class GroupWithUsersDTO {

    private String groupName;
    private String description;
    private List<String> emails;

    public GroupWithUsersDTO(String groupName, String description, List<String> emails) {
        this.groupName = groupName;
        this.description = description;
        this.emails = emails;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getEmails() {
        return emails;
    }

    public void setEmail(List<String> emails) {
        this.emails = emails;
    }
}
