package org.taskflow.DTO;

import java.util.List;

public class GroupRequestDTO {

    private int groupId;
    private String groupName;
    private String description;
    private List<String> emails;

    public GroupRequestDTO() {}

    public GroupRequestDTO(int groupId, String groupName, String description, List<String> emails) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.description = description;
        this.emails = emails;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
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

    public void setEmails(List<String> emails) {
        this.emails = emails;
    }
}
