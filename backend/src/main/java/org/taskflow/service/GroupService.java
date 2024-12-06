package org.taskflow.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.taskflow.model.*;
import org.taskflow.repository.GroupRepository;
import org.taskflow.repository.TaskGroupRepository;
import org.taskflow.repository.UserGroupRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class GroupService {

    private TaskService taskService;
    private UserGroupRepository userGroupRepository;
    private UserGroupService userGroupService;
    private UserService userService;
    private GroupRepository groupRepository;
    private TaskGroupRepository taskGroupRepository;

    @Autowired
    @Lazy
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Autowired
    public void setUserGroupRepository(UserGroupRepository userGroupRepository) {
        this.userGroupRepository = userGroupRepository;
    }
    @Autowired
    @Lazy
    public void setUserGroupService(UserGroupService userGroupService) {
        this.userGroupService = userGroupService;
    }


    @Autowired
    public void setUserDataService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setGroupRepository(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    @Autowired
    public void setTaskGroupRepository(TaskGroupRepository taskGroupRepository) {
        this.taskGroupRepository = taskGroupRepository;
    }

    public ResponseEntity<String> createGroupWithUsers(Group group, List<String> emails, int ownerId) {
        try {
            groupRepository.save(group);

            User owner = userService.getUserById(ownerId);
            if (!emails.contains(owner.getEmail())) {
                emails.add(owner.getEmail());
            }

            emails.stream()
                    .map(userService::getUserByEmail)
                    .filter(Objects::nonNull)
                    .forEach(user ->
                            userGroupService.addUserGroup(group.getGroupId(), user.getUserId())
                    );

            return ResponseEntity.ok("Group created successfully");
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("There was an error processing your request: " + e.getMessage());
        }
    }

    public ResponseEntity<String> createGroup(Group group) {
        try {
            groupRepository.save(group);
            return ResponseEntity.ok("Group created successfully");
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("There was an error processing your request: " + e.getMessage());
        }
    }


    public ResponseEntity<String> addUserToGroup(int groupId, String email) {
        try {
            if (!userService.isValidUser(email) || !isValidGroup(groupId)) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Invalid user ID or Group ID");
            }

            if (userGroupService.isUserInGroup(groupId, email)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("User is already in group");
            }

            User user = userService.getUserByEmail(email);
            Group group = getGroupById(groupId);
            UserGroup userGroup = new UserGroup(user, group);
            UserGroupKey userGroupKey = new UserGroupKey(user.getUserId(), groupId);
            userGroup.setId(userGroupKey);
            userGroupRepository.save(userGroup);

            return ResponseEntity.ok("User successfully added to group");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("There was an error processing your request: " + e.getMessage());
        }
    }

    public ResponseEntity<String> removeUserFromGroup(int groupId, int userId) {
        try {
            if (!userService.isValidUser(userId) && !isValidGroup(groupId)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Invalid user ID or Group ID");
            }

            if (!userGroupService.isUserInGroup(groupId, userId)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("User not in group");
            }

            User user = userService.getUserById(userId);
            Group group = getGroupById(groupId);

            userGroupRepository.findByUserAndGroup(user, group)
                    .stream()
                    .findFirst()
                    .ifPresentOrElse(userGroupRepository::delete,
                            () -> { throw new IllegalStateException("UserGroup not found despite validation.");
                    });

            return ResponseEntity.ok("User successfully deleted from group");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("There was an error processing your request: " + e.getMessage());
        }
    }

    public List<UserGroup> getGroupsByUserId(int userId) {
        if (!userService.isValidUser(userId)) {
            return new ArrayList<>();
        }
        User user = userService.getUserById(userId);
        return userGroupRepository.findByUser(user, Sort.by(Sort.Direction.ASC, "createdAt"));
    }

    public TaskGroup getGroupByUserIdAndTaskId(int userId, int taskId) {
        if(userService.isValidUser(userId) && isValidGroup(taskId)) {
            User user = userService.getUserById(userId);
            Task task = taskService.getTaskById(taskId);

            List<UserGroup> userGroup = userGroupRepository
                    .findByUser(user, Sort.by(Sort.Direction.ASC, "createdAt"));

            for (UserGroup group : userGroup) {
                List<TaskGroup> taskGroups = taskGroupRepository.findByGroup(group.getGroup());
                for(TaskGroup taskGroup : taskGroups){
                    if(taskGroup.getTask().equals(task)){
                        return taskGroup;
                    }
                }
            }
        }
        return null;
    }

    public List<UserGroup> getUsersByGroup(int groupId) {
        if(!isValidGroup(groupId)) {
            return new ArrayList<>();
        }
        Group group = getGroupById(groupId);
        return userGroupRepository.findByGroup(group);
    }

    public List<TaskGroup> getGroupsByTask(int taskId) {
        if(!taskService.isValidTask(taskId)) {
            return new ArrayList<>();
        }
        Task task = taskService.getTaskById(taskId);
        return taskGroupRepository.findByTask(task);
    }

    public List<TaskGroup> getTaskByGroup(int groupId) {
        if (!isValidGroup(groupId)) {
            return new ArrayList<>();
        }
        Group group = getGroupById(groupId);
        return taskGroupRepository.findByGroup(group);
    }


    public Group getGroupById(int groupId){
        if(!isValidGroup(groupId)) {
            return null;
        }
        return groupRepository.findByGroupId(groupId).getFirst();
    }

    public List<Group> getGroupByNameContaing(String groupName){
        return groupRepository.findByGroupNameContainingIgnoreCase(groupName);
    }

    public List<Group> getGroupByName(String groupName){
        return groupRepository.findByGroupName(groupName);
    }


    public boolean isValidGroup(int groupId){
        List<Group> groups = groupRepository.findByGroupId(groupId);
        return !groups.isEmpty();
    }
}