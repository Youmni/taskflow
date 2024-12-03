package org.taskflow.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.taskflow.model.Group;
import org.taskflow.model.Permission;
import org.taskflow.model.Task;
import org.taskflow.model.TaskGroup;
import org.taskflow.repository.GroupRepository;
import org.taskflow.repository.TaskGroupRepository;
import org.taskflow.repository.TaskRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class TaskGroupService {

    private TaskGroupRepository taskGroupRepository;
    private GroupService groupService;
    private TaskService taskService;

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

    public void createTaskGroup(int taskId, int groupId, Permission permission) {
        try {
            if (taskService.isValidTask(taskId) && groupService.isValidGroup(groupId)) {
                Task task = taskService.getTaskById(taskId);
                Group group = groupService.getGroupById(groupId);

                if(group == null) {
                    throw new Exception("Group not found");
                }

                TaskGroup taskGroup = new TaskGroup(task, group, permission);
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

            Permission groupPermission = taskGroups.getFirst().getPermission();
            return groupPermission.includes(Permission.WRITE);
        }else{
            return false;
        }
    }
}