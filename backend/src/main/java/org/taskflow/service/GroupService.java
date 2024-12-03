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

@Service
public class GroupService {

    private TaskService taskService;
    private UserGroupRepository userGroupRepository;
    private UserGroupService userGroupService;
    private UserDataService userDataService;
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
    public void setUserDataService(UserDataService userDataService) {
        this.userDataService = userDataService;
    }

    @Autowired
    public void setGroupRepository(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    @Autowired
    public void setTaskGroupRepository(TaskGroupRepository taskGroupRepository) {
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
            if (userDataService.isValidUser(userId) && isValidGroup(groupId)) {
                if (userGroupService.isUserInGroup(groupId, userId)) {
                    User user = userDataService.getUserById(userId);
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
            if (userDataService.isValidUser(userId) && isValidGroup(groupId)) {
                if (userGroupService.isUserInGroup(groupId, userId)) {
                    User user = userDataService.getUserById(userId);
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
        if (userDataService.isValidUser(userId)) {
            User user = userDataService.getUserById(userId);
            return userGroupRepository.findByUser(user, Sort.by(Sort.Direction.ASC, "createdAt"));
        }else{
            return new ArrayList<>();
        }
    }

    public TaskGroup getGroupByUserIdAndTaskId(int userId, int taskId) {
        if(userDataService.isValidUser(userId) && isValidGroup(taskId)) {
            User user = userDataService.getUserById(userId);
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
        if(isValidGroup(groupId)) {
            Group group = getGroupById(groupId);
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
        if (isValidGroup(groupId)) {
            Group group = getGroupById(groupId);
            return taskGroupRepository.findByGroup(group);
        }else{
            return new ArrayList<>();
        }
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