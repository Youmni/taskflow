package org.taskflow.service;

import org.springframework.beans.factory.annotation.Autowired;
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

@Service
public class GroupService {

    private final TaskService taskService;
    private final UserGroupRepository userGroupRepository;
    private final UserGroupService userGroupService;
    private final GroupService groupService;
    private final UserService userService;
    private final GroupRepository groupRepository;
    private final TaskGroupRepository taskGroupRepository;

    @Autowired
    public GroupService(TaskService taskService, UserGroupRepository userGroupRepository, UserGroupService userGroupService, GroupService groupService, UserService userService, GroupRepository groupRepository, TaskGroupRepository taskGroupRepository) {
        this.taskService = taskService;
        this.userGroupRepository = userGroupRepository;
        this.userGroupService = userGroupService;
        this.groupService = groupService;
        this.userService = userService;
        this.groupRepository = groupRepository;
        this.taskGroupRepository = taskGroupRepository;
    }

    public ResponseEntity<String> createGroup(Group group, List<User> users) {
        try {
            groupRepository.save(group);
            for (User user : users) {
                userGroupService.addUserGroup(group.getGroupId(), user.getUserId());
            }
            return ResponseEntity.ok("Group created successfully");
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("There was an error processing your request: " + e.getMessage());
        }
    }


    public ResponseEntity<String> addUserToGroup(int groupId, int userId) {
        try {
            if (userService.isValidUser(userId) && isValidGroup(groupId)) {
                if (userGroupService.isUserInGroup(groupId, userId)) {
                    User user = userService.getUserById(userId);
                    Group group = getGroupById(groupId);
                    UserGroup userGroup = new UserGroup(user, group);
                    userGroupRepository.save(userGroup);
                    return ResponseEntity.ok("User successfully added to group");
                }else{
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body("User is already in group");
                }

            }else{
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Invalid user ID or Group ID");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("There was an error processing your request: " + e.getMessage());
        }
    }

    public ResponseEntity<String> removeUserFromGroup(int groupId, int userId) {
        try {
            if (userService.isValidUser(userId) && isValidGroup(groupId)) {
                if (userGroupService.isUserInGroup(groupId, userId)) {
                    User user = userService.getUserById(userId);
                    Group group = getGroupById(groupId);
                    List<UserGroup> userGroups = userGroupRepository.findByUserAndGroup(user, group);
                    userGroupRepository.delete(userGroups.getFirst());
                    return ResponseEntity.ok("User successfully deleted from group");
                }else{
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body("User not in group");
                }

            }else{
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Invalid user ID or Group ID");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("There was an error processing your request: " + e.getMessage());
        }
    }
    public List<UserGroup> getGroupsByUser(int userId) {
        if (userService.isValidUser(userId)) {
            User user = userService.getUserById(userId);
            return userGroupRepository.findByUser(user, Sort.by(Sort.Direction.ASC, "createdAt"));
        }else{
            return new ArrayList<>();
        }
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
        if(groupService.isValidGroup(groupId)) {
            Group group = groupService.getGroupById(groupId);
            return userGroupRepository.findByGroup(group);
        }else{
            return new ArrayList<>();
        }
    }

    public List<TaskGroup> getGroupsByTask(int taskId) {
        if(taskService.isValidTask(taskId)) {
            Task task = taskService.getTaskById(taskId);
            return taskGroupRepository.findByTask(task);
        }else{
            return new ArrayList<>();
        }
    }

    public List<TaskGroup> getTaskByGroup(int groupId) {
        if (groupService.isValidGroup(groupId)) {
            Group group = groupService.getGroupById(groupId);
            return taskGroupRepository.findByGroup(group);
        }else{
            return new ArrayList<>();
        }
    }


    public Group getGroupById(int groupId){
        if(!groupService.isValidGroup(groupId)) {
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
