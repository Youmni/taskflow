package org.taskflow.DTO;

public class GroupDTO {

    private String groupName;
    private String description;

    public GroupDTO(String description, String groupName) {
        this.description = description;
        this.groupName = groupName;
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
}
