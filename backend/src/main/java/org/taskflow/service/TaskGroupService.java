package org.taskflow.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.taskflow.model.*;
import org.taskflow.repository.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class TaskGroupService {

    private TaskGroupRepository taskGroupRepository;
    private GroupService groupService;
    private TaskService taskService;
    private UserGroupRepository userGroupRepository;

    @Autowired
    public void setTaskGroupRepository(TaskGroupRepository taskGroupRepository) {
        this.taskGroupRepository = taskGroupRepository;
    }

    @Autowired
    public void setGroupService(GroupService groupService) {
        this.groupService = groupService;
    }

    @Autowired
    @Lazy
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Autowired
    public void setUserGroupRepository(UserGroupRepository userGroupRepository) {
        this.userGroupRepository = userGroupRepository;
    }

    public void createTaskGroup(int taskId, int groupId, Permission permission) {
        try {
            if (taskService.isValidTask(taskId) && groupService.isValidGroup(groupId)) {
                Task task = taskService.getTaskById(taskId);
                Group group = groupService.getGroupById(groupId);

                if(group == null) {
                    throw new Exception("Group not found");
                }
                TaskGroupKey taskGroupKey = new TaskGroupKey(taskId, groupId);
                TaskGroup taskGroup = new TaskGroup(task, group, permission);
                taskGroup.setId(taskGroupKey);
                taskGroupRepository.save(taskGroup);
                ResponseEntity.status(HttpStatus.CREATED)
                        .body("Task group successfully created");
            } else {
                ResponseEntity.badRequest().body("Invalid task or group");
            }
        }catch (Exception e) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("There was an error processing your request: " + e.getMessage());
        }
    }

    public ResponseEntity<String> deleteTaskGroup(int taskId, int groupId) {
        try{
            if (taskService.isValidTask(taskId) && groupService.isValidGroup(groupId)) {
                Task task = taskService.getTaskById(taskId);
                Group group = groupService.getGroupById(groupId);

                if(group == null) {
                    return ResponseEntity.badRequest().body("Group not found");
                }

                List<TaskGroup> taskGroups = taskGroupRepository.findByTaskAndGroup(task, group);
                taskGroupRepository.delete(taskGroups.getFirst());
                return ResponseEntity.ok()
                        .body("Task group successfully deleted");
            }else{
                return ResponseEntity.badRequest()
                        .body("Invalid task or group");
            }
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("There was an error processing your request: " + e.getMessage());
        }
    }

    public ResponseEntity<String> updateTaskGroupPermission(int taskId, int groupId, Permission permission) {
        try{
            if (taskService.isValidTask(taskId) && groupService.isValidGroup(groupId)) {
                Task task = taskService.getTaskById(taskId);
                Group group = groupService.getGroupById(groupId);

                if(group == null || task == null) {
                    return ResponseEntity.badRequest().body("Group or task not found");
                }

                TaskGroup taskGroup
                        = taskGroupRepository.findByTaskAndGroup(task, group).getFirst();

                taskGroup.setPermission(permission);
                taskGroupRepository.save(taskGroup);
                return ResponseEntity.ok()
                        .body("Task group successfully updated");
            }else{
                return ResponseEntity.badRequest()
                        .body("Invalid task or group");
            }
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("There was an error processing your request: " + e.getMessage());
        }
    }

    public List<TaskGroup> getGroupsByTaskId(int taskId) {
        if (taskService.isValidTask(taskId)) {
            Task task = taskService.getTaskById(taskId);
            return taskGroupRepository.findByTask(task);
        } else {
            return new ArrayList<>();
        }
    }


    public List<TaskGroup> getTasksByGroupsId(int groupId) {
        if (groupService.isValidGroup(groupId)) {
            Group group = groupService.getGroupById(groupId);
            if(group == null){
                return new ArrayList<>();
            }

            return taskGroupRepository.findByGroup(group);
        }else{
            return new ArrayList<>();
        }
    }

    public boolean isAllowedToDelete(Permission permission, int taskId) {
        if(taskService.isValidTask(taskId)) {
            Task task = taskService.getTaskById(taskId);
            List<TaskGroup> taskGroups = taskGroupRepository.findByTask(task);

            if(task == null || taskGroups.isEmpty()){
                return false;
            }

            Permission groupPermission = taskGroups.getFirst().getPermission();
            return groupPermission.includes(Permission.DELETE);
        }else{
            return false;
        }
    }

    public boolean isAllowedToWrite(Permission permission, int taskId) {
        if(taskService.isValidTask(taskId)) {
            Task task = taskService.getTaskById(taskId);
            List<TaskGroup> taskGroups = taskGroupRepository.findByTask(task);

            if(task == null || taskGroups.isEmpty()){
                return false;
            }

            return permission.includes(Permission.WRITE);
        }else{
            return false;
        }
    }

    public HashMap<String, Permission> getEmailsAndPermissionsByTaskId(int taskId) {
        if(!taskService.isValidTask(taskId)) {
            throw new RuntimeException("taskId is not valid");
        }

        Task task = taskService.getTaskById(taskId);
        List<TaskGroup> taskGroups = taskGroupRepository.findByTask(task);
        System.out.println(taskGroups.size());
        HashMap<String, Permission> emailsAndPermissions = new HashMap<>();

        for(TaskGroup taskGroup : taskGroups){
            System.out.println("in loop yooo");
            List<UserGroup> userGroupList = userGroupRepository.findByGroup(taskGroup.getGroup());
            for(UserGroup userGroupUser : userGroupList){
                emailsAndPermissions.put(userGroupUser.getUser().getEmail(), taskGroup.getPermission());
            }
        }

        return emailsAndPermissions;
    }
}