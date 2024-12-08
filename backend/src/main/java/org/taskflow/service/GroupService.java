package org.taskflow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.taskflow.DTO.GroupDTO;
import org.taskflow.DTO.GroupRequestDTO;
import org.taskflow.model.*;
import org.taskflow.repository.GroupRepository;
import org.taskflow.repository.TaskGroupRepository;
import org.taskflow.repository.UserGroupRepository;
import org.taskflow.wrapper.UserGroupRequest;

import java.util.*;

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

    public ResponseEntity<String> createGroup(GroupDTO groupDTO, int ownerId) {
        try {
            if(!userService.isValidUser(ownerId)){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("There was an incorrect owner id");
            }
            User owner = userService.getUserById(ownerId);
            Group group = new Group(groupDTO.getGroupName(), groupDTO.getDescription(), owner);
            groupRepository.save(group);
            return ResponseEntity.ok("Group created successfully");
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("There was an error processing your request: " + e.getMessage());
        }
    }


    public ResponseEntity<String> deleteGroup(int groupId, int ownerId) {

        if(!isUserOwnerOfGroup(ownerId, groupId)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid Group ID");
        }

        if(!isValidGroup(groupId)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Group ID");
        }
        Group group = getGroupById(groupId);

        List<UserGroup> userGroups = userGroupRepository.findByGroup(group);
        List<TaskGroup> taskGroups = taskGroupRepository.findByGroup(group);

        userGroupRepository.deleteAll(userGroups);
        taskGroupRepository.deleteAll(taskGroups);

        groupRepository.deleteById(groupId);
        return ResponseEntity.ok("Group successfully deleted");
    }


    public ResponseEntity<String> addUserToGroup(int groupId, String email, int ownerId) {
        try {
            if (!userService.isValidUser(email) || !isValidGroup(groupId)) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Invalid user ID or Group ID");
            }

            if (userGroupService.isUserInGroup(groupId, email)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("User is already in group");
            }

            if(!isUserOwnerOfGroup(ownerId, groupId)){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("User is not owner of group");
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

    public ResponseEntity<String> addUsersToGroup(int groupId, List<String> emails, int ownerId) {
        try{
            if (!isValidGroup(groupId)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Invalid Group ID");
            }

            List<String> successes = new ArrayList<>();
            List<String> failures = new ArrayList<>();

            for(String email: emails){
                ResponseEntity<String> response = addUserToGroup(groupId, email, ownerId);
                if(response.getStatusCode() == HttpStatus.OK){
                    successes.add(email+": "+response.getBody());
                }else{
                    failures.add(email+": "+response.getBody());
                }
            }
            Map<String, Object> result = new HashMap<>();
            result.put("Successes",successes);
            result.put("Failures",failures);

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonResponse = objectMapper.writeValueAsString(result);

            return ResponseEntity.ok(jsonResponse);
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("There was an error processing your request: " + e.getMessage());
        }
    }


    public ResponseEntity<String> removeUserFromGroup(int groupId, String email, int ownerId) {
        try {
            if (!userService.isValidUser(email) && !isValidGroup(groupId)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Invalid user ID or Group ID");
            }

            if (!userGroupService.isUserInGroup(groupId, email)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("User not in group");
            }

            if(!isUserOwnerOfGroup(ownerId, groupId)){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("User is not owner of group");
            }

            User user = userService.getUserByEmail(email);
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

    public ResponseEntity<String> removeUsersFromGroup(int groupId, List<String> emails, int ownerId) {
        try{
            if (!isValidGroup(groupId)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Invalid Group ID");
            }

            List<String> successes = new ArrayList<>();
            List<String> failures = new ArrayList<>();

            for(String email: emails){
                ResponseEntity<String> response = removeUserFromGroup(groupId, email, ownerId);
                if(response.getStatusCode() == HttpStatus.OK){
                    successes.add(email+": "+response.getBody());
                }else{
                    failures.add(email+": "+response.getBody());
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("Successes",successes);
            result.put("Failures",failures);

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonResponse = objectMapper.writeValueAsString(result);

            return ResponseEntity.ok(jsonResponse);

        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("There was an error processing your request: " + e.getMessage());
        }
    }


    public List<GroupRequestDTO> getGroupsByUserId(int userId) {
        if (!userService.isValidUser(userId)) {
            System.out.println("user is not valid");
            return new ArrayList<>();
        }
        User user = userService.getUserById(userId);
        List<Group> groupList = groupRepository.findByCreatedBy(user, Sort.by(Sort.Direction.ASC, "createdAt"));
        List<GroupRequestDTO> groupRequestDTOList = new ArrayList<>();

        for(Group group : groupList) {
            GroupRequestDTO groupRequestDTO = new GroupRequestDTO();
            groupRequestDTO.setGroupId(group.getGroupId());
            groupRequestDTO.setGroupName(group.getGroupName());
            groupRequestDTO.setDescription(group.getDescription());

            List<String> emails = userGroupService.getEmailsByGroupId(group.getGroupId());
            groupRequestDTO.setEmails(emails);

            groupRequestDTOList.add(groupRequestDTO);
        }
        return groupRequestDTOList;
    }

    public TaskGroup getGroupByUserIdAndTaskId(int userId, int taskId) {
        if(userService.isValidUser(userId) && taskService.isValidTask(taskId)) {
            User user = userService.getUserById(userId);
            Task task = taskService.getTaskById(taskId);

            List<UserGroup> userGroup = userGroupRepository.findByUser(user);

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

    public boolean isUserOwnerOfGroup(int ownerId, int groupId) {
        if(!isValidGroup(groupId)) {
            return false;
        }
        Group group = getGroupById(groupId);
        return group.getCreatedBy().getUserId() == ownerId;
    }
}