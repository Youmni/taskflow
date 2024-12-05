package org.taskflow.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "`group`")
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private int groupId;

    @NotBlank(message = "Groupname is required")
    @Size(min = 5, max = 20, message = "Groupname must be between 5 and 20 characters")
    @Column(name = "group_name")
    private String groupName;

    @Size(max = 80, message = "Description must be between 0 and 80 characters")
    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "group")
    Set<TaskGroup> taskGroups;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected Group() {}

    public Group(String groupName, String description) {
        this.groupName = groupName;
        this.description = description;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public @NotBlank(message = "Groupname is required") @Size(min = 5, max = 20, message = "Groupname must be between 5 and 20 characters") String getGroupName() {
        return groupName;
    }

    public void setGroupName(@NotBlank(message = "Groupname is required") @Size(min = 5, max = 20, message = "Groupname must be between 5 and 20 characters") String groupName) {
        this.groupName = groupName;
    }

    public @Size(min = 0, max = 80, message = "Description must be between 0 and 80 characters") String getDescription() {
        return description;
    }

    public void setDescription(@Size(min = 0, max = 80, message = "Description must be between 0 and 80 characters") String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Set<TaskGroup> getTaskGroups() {
        return taskGroups;
    }

    public void setTaskGroups(Set<TaskGroup> taskGroups) {
        this.taskGroups = taskGroups;
    }
}
