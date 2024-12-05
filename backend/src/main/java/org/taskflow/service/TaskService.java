package org.taskflow.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.taskflow.model.*;
import org.taskflow.repository.TaskGroupRepository;
import org.taskflow.repository.TaskRepository;
import org.taskflow.repository.UserGroupRepository;
import org.taskflow.wrapper.TaskCreationRequest;
import org.taskflow.wrapper.TaskRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TaskService {

    private static final String ERROR_MESSAGE = "There was an error processing your request: ";
    private static final String ERROR_WRITE_PERMISSION ="You do not have permission to write to this task";
    private UserService userService;
    private TaskHistoryService taskHistoryService;
    private TaskGroupService taskGroupService;

    @Autowired
    @Lazy
    private GroupService groupService;

    @Autowired
    @Lazy
    private TaskRepository taskRepository;
    @Autowired
    private UserGroupRepository userGroupRepository;
    @Autowired
    private TaskGroupRepository taskGroupRepository;

    @Autowired
    public void setUserDataService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    @Lazy
    public void setTaskHistoryService(TaskHistoryService taskHistoryService) {
        this.taskHistoryService = taskHistoryService;
    }

    @Autowired
    @Lazy
    public void setTaskGroupService(TaskGroupService taskGroupService) {
        this.taskGroupService = taskGroupService;
    }

    @Autowired
    public void setGroupService(GroupService groupService) {
        this.groupService = groupService;
    }

    @Autowired
    public void setTaskRepository(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public ResponseEntity<String> createTask(TaskCreationRequest taskCreationRequest) {
        try {

            TaskRequest taskRequest = taskCreationRequest.getTaskRequest();
            HashMap<Integer, Permission> groups = taskCreationRequest.getGroup();

            Task task = new Task(
                    taskRequest.getTitle(),
                    taskRequest.getDescription(),
                    taskRequest.getStatus(),
                    taskRequest.getPriority(),
                    taskRequest.getDueDate(),
                    taskRequest.getComment(),
                    null
            );
            if(!userService.isValidUser(taskRequest.getUserId())){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The user ID given with the task does not exist");
            }
            User user = userService.getUserById(taskRequest.getUserId());
            task.setUser(user);

            taskRepository.save(task);
            taskHistoryService.createTaskHistory(task, task.getUser().getUserId());

            for (Map.Entry<Integer, Permission> entry : groups.entrySet()) {

                Integer group = entry.getKey();
                Permission permission = entry.getValue();
                taskGroupService.createTaskGroup(task.getTaskId(), group, permission);
            }

            return ResponseEntity.ok("Task created successfully");
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("There was an error processing your request: " + e.getMessage());
        }
    }

    public ResponseEntity<String> updateTask(int taskId, Task task, int userIdMakingChanges) {

        if (!taskRepository.existsByTaskId(taskId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task not found");
        }

        if(!isWritePermissionGranted(userIdMakingChanges, taskId)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to update this task");
        }

        try {
            List<Task> tasks = taskRepository.findByTaskId(taskId);


            Task taskToUpdate = tasks.getFirst();

            taskToUpdate.setTitle(task.getTitle());
            taskToUpdate.setDescription(task.getDescription());
            taskToUpdate.setStatus(task.getStatus());
            taskToUpdate.setPriority(task.getPriority());
            taskToUpdate.setDueDate(task.getDueDate());
            taskToUpdate.setComment(task.getComment());
            taskRepository.save(taskToUpdate);

            return taskHistoryService.createTaskHistory(taskToUpdate, userIdMakingChanges);
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR_MESSAGE + e.getMessage());
        }
    }



    public ResponseEntity<String> deleteTask(int taskId, int userId) {
        //check if user can delete
        if(!isDeletePermissionGranted(userId, taskId)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to delete this task");
        }
        if (taskRepository.existsByTaskId(taskId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task not found");
        }
        try{
            taskRepository.deleteById(taskId);
            return ResponseEntity.ok("Task deleted successfully");
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR_MESSAGE + e.getMessage());
        }
    }

    public ResponseEntity<String> updateTitle(int taskId, String title, int userIdMakingChanges) {
        //check if the user can write
        if(!isWritePermissionGranted(userIdMakingChanges, taskId)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ERROR_WRITE_PERMISSION);
        }

        if (taskRepository.existsByTaskId(taskId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task not found");
        }
        try{
            List<Task> tasks = taskRepository.findByTaskId(taskId);

            Task taskToUpdate = tasks.getFirst();
            taskToUpdate.setTitle(title);
            taskRepository.save(taskToUpdate);
            taskHistoryService.createTaskHistory(taskToUpdate, userIdMakingChanges);
            return ResponseEntity.ok("Task updated successfully");
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR_MESSAGE + e.getMessage());
        }
    }

    public ResponseEntity<String> updateDescription(int taskId, String description, int userIdMakingChanges) {
        //check if the user can write
        if(!isWritePermissionGranted(userIdMakingChanges, taskId)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ERROR_WRITE_PERMISSION);
        }

        if (taskRepository.existsByTaskId(taskId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task not found");
        }
        try{
            List<Task> tasks = taskRepository.findByTaskId(taskId);

            Task taskToUpdate = tasks.getFirst();
            taskToUpdate.setDescription(description);
            taskRepository.save(taskToUpdate);
            taskHistoryService.createTaskHistory(taskToUpdate, userIdMakingChanges);
            return ResponseEntity.ok("Task updated successfully");
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR_MESSAGE + e.getMessage());
        }
    }

    public ResponseEntity<String> updateStatus(int taskId, Status status, int userIdMakingChanges) {
        //check if the user can write
        if(!isWritePermissionGranted(userIdMakingChanges, taskId)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ERROR_WRITE_PERMISSION);
        }

        if (taskRepository.existsByTaskId(taskId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task not found");
        }
        try{
            List<Task> tasks = taskRepository.findByTaskId(taskId);

            Task taskToUpdate = tasks.getFirst();
            taskToUpdate.setStatus(status);
            taskRepository.save(taskToUpdate);
            taskHistoryService.createTaskHistory(taskToUpdate, userIdMakingChanges);
            return ResponseEntity.ok("Task updated successfully");
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR_MESSAGE + e.getMessage());
        }
    }

    public ResponseEntity<String> updatePriority(int taskId, Priority priority, int userIdMakingChanges) {
        //check if the user can write
        if(!isWritePermissionGranted(userIdMakingChanges, taskId)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ERROR_WRITE_PERMISSION);
        }

        if (taskRepository.existsByTaskId(taskId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task not found");
        }
        try{
            List<Task> tasks = taskRepository.findByTaskId(taskId);

            Task taskToUpdate = tasks.getFirst();
            taskToUpdate.setPriority(priority);
            taskRepository.save(taskToUpdate);
            taskHistoryService.createTaskHistory(taskToUpdate, userIdMakingChanges);
            return ResponseEntity.ok("Task updated successfully");
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR_MESSAGE + e.getMessage());
        }
    }

    public ResponseEntity<String> updateDueDate(int taskId, LocalDate dueDate, int userIdMakingChanges) {
        //check if the user can write
        if(!isWritePermissionGranted(userIdMakingChanges, taskId)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ERROR_WRITE_PERMISSION);
        }

        if (taskRepository.existsByTaskId(taskId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task not found");
        }
        try{
            List<Task> tasks = taskRepository.findByTaskId(taskId);

            Task taskToUpdate = tasks.getFirst();
            taskToUpdate.setDueDate(dueDate);
            taskRepository.save(taskToUpdate);
            taskHistoryService.createTaskHistory(taskToUpdate, userIdMakingChanges);
            return ResponseEntity.ok("Task updated successfully");
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR_MESSAGE + e.getMessage());
        }
    }

    public ResponseEntity<String> updateComment(int taskId, String comment, int userIdMakingChanges) {
        //check if the user can write
        if(!isWritePermissionGranted(userIdMakingChanges, taskId)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ERROR_WRITE_PERMISSION);
        }

        if (taskRepository.existsByTaskId(taskId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task not found");
        }
        try{
            List<Task> tasks = taskRepository.findByTaskId(taskId);

            Task taskToUpdate = tasks.getFirst();
            taskToUpdate.setComment(comment);
            taskRepository.save(taskToUpdate);
            taskHistoryService.createTaskHistory(taskToUpdate, userIdMakingChanges);
            return ResponseEntity.ok("Task updated successfully");
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR_MESSAGE + e.getMessage());
        }
    }

    public List<Task> getTasks() {
        return taskRepository.findAll();
    }

    public Task getTaskById(int taskId) {
        return taskRepository.findByTaskId(taskId).getFirst();
    }


    public List<Task> getTasksForUser(int userId){
        User user = userService.getUserById(userId);
        List<UserGroup> userGroups = userGroupRepository.findByUser(user, Sort.by(Sort.Direction.DESC, "createdAt"));

        List<Task> tasks = new ArrayList<>();

        for(UserGroup userGroup : userGroups){
            Group group = userGroup.getGroup();

            List<TaskGroup> taskGroups = taskGroupRepository.findByGroup(group);

            for(TaskGroup taskGroup : taskGroups){
                Task task = taskGroup.getTask();
                tasks.add(task);
            }
        }
        return tasks;
    }

    public List<Task> getTasksForUserByDueDate(int userId, LocalDate dueDate) {
        User user = userService.getUserById(userId);
        List<UserGroup> userGroups = userGroupRepository.findByUser(user, Sort.by(Sort.Direction.DESC, "createdAt"));

        List<Task> tasks = new ArrayList<>();

        for(UserGroup userGroup : userGroups){
            Group group = userGroup.getGroup();

            List<TaskGroup> taskGroups = taskGroupRepository.findByGroup(group);

            for(TaskGroup taskGroup : taskGroups){
                Task task = taskGroup.getTask();

                if(task.getDueDate()!= null && task.getDueDate().isEqual(dueDate)) {
                    tasks.add(task);
                }
            }
        }
        return tasks;
    }

    public List<Task> getTasksForUserByBeforeDueDate(int userId, LocalDate dueDate) {
        User user = userService.getUserById(userId);
        List<UserGroup> userGroups = userGroupRepository.findByUser(user, Sort.by(Sort.Direction.DESC, "createdAt"));

        List<Task> tasks = new ArrayList<>();

        for(UserGroup userGroup : userGroups){
            Group group = userGroup.getGroup();

            List<TaskGroup> taskGroups = taskGroupRepository.findByGroup(group);

            for(TaskGroup taskGroup : taskGroups){
                Task task = taskGroup.getTask();

                if(task.getDueDate()!= null && task.getDueDate().isBefore(dueDate)) {
                    tasks.add(task);
                }
            }
        }
        return tasks;
    }
    public List<Task> getTasksForUserByAfterDueDate(int userId, LocalDate dueDate) {
        User user = userService.getUserById(userId);
        List<UserGroup> userGroups = userGroupRepository.findByUser(user, Sort.by(Sort.Direction.DESC, "createdAt"));

        List<Task> tasks = new ArrayList<>();

        for(UserGroup userGroup : userGroups){
            Group group = userGroup.getGroup();

            List<TaskGroup> taskGroups = taskGroupRepository.findByGroup(group);

            for(TaskGroup taskGroup : taskGroups){
                Task task = taskGroup.getTask();

                if(task.getDueDate()!= null && task.getDueDate().isAfter(dueDate)) {
                    tasks.add(task);
                }
            }
        }
        return tasks;
    }

    public List<Task> getTasksForUsersByPriority(int userId, Priority priority) {
        User user = userService.getUserById(userId);
        List<UserGroup> userGroups = userGroupRepository.findByUser(user, Sort.by(Sort.Direction.DESC, "createdAt"));

        List<Task> tasks = new ArrayList<>();

        for(UserGroup userGroup : userGroups){
            Group group = userGroup.getGroup();

            List<TaskGroup> taskGroups = taskGroupRepository.findByGroup(group);

            for(TaskGroup taskGroup : taskGroups){
                Task task = taskGroup.getTask();

                if(task.getPriority()!= null && task.getPriority().equals(priority)) {
                    tasks.add(task);
                }
            }
        }
        return tasks;
    }

    public List<Task> getTasksForUsersByStatus(int userId, Status status) {
        User user = userService.getUserById(userId);
        List<UserGroup> userGroups = userGroupRepository.findByUser(user, Sort.by(Sort.Direction.DESC, "createdAt"));

        List<Task> tasks = new ArrayList<>();

        for(UserGroup userGroup : userGroups){
            Group group = userGroup.getGroup();

            List<TaskGroup> taskGroups = taskGroupRepository.findByGroup(group);

            for(TaskGroup taskGroup : taskGroups){
                Task task = taskGroup.getTask();

                if(task.getStatus()!= null && task.getStatus().equals(status)) {
                    tasks.add(task);
                }
            }
        }
        return tasks;
    }


    public List<Task> getTaskByTitle(String title) {
        return taskRepository.findByTitle(title);
    }

    public List<Task> getTaskByTitleContaining(String title) {
        return taskRepository.findByTitleContainingIgnoreCase(title);
    }

    public List<Task> getTaskByDueDate(LocalDate dueDate, Sort sort) {
        Sort finalSort = (sort == null) ? Sort.by(Sort.Order.asc("title")) : sort;
        return taskRepository.findByDueDate(dueDate, finalSort);
    }

    public List<Task> getTaskByDueDateBefore(LocalDate dueDate, Sort sort) {
        Sort finalSort = (sort == null) ? Sort.by(Sort.Order.asc("dueDate")) : sort;
        return taskRepository.findByDueDateBefore(dueDate, finalSort);
    }

    public List<Task> getTaskByDueDateAfter(LocalDate dueDate, Sort sort) {
        Sort finalSort = (sort == null) ? Sort.by(Sort.Order.asc("dueDate")) : sort;
        return taskRepository.findByDueDateAfter(dueDate, finalSort);
    }

    public List<Task> getTaskByDueDateBetween(LocalDate from, LocalDate to, Sort sort) {
        Sort finalSort = (sort == null) ? Sort.by(Sort.Order.asc("dueDate")) : sort;
        return taskRepository.findByDueDateBetween(from, to, finalSort);
    }

    public List<Task> getTaskByUserid(int userId, Sort sort) {

        User user = userService.getUserById(userId);
        Sort finalSort = (sort == null) ? Sort.by(Sort.Order.asc("dueDate")) : sort;
        return taskRepository.findByUser(user, finalSort);
    }

    public List<Task> getTaskByStatus(Status status, Sort sort) {
        Sort finalSort = (sort == null) ? Sort.by(Sort.Order.asc("dueDate")) : sort;
        return taskRepository.findByStatus(status, finalSort);
    }

    public List<Task> getTaskByStatusMultiple(List<Status> status, Sort sort) {
        Sort finalSort = (sort == null) ? Sort.by(Sort.Order.asc("status"), Sort.Order.asc("dueDate")) : sort;
        return taskRepository.findByStatusIn(status, finalSort);
    }


    public List<Task> getTaskByPriority(Priority priority, Sort sort) {
        Sort finalSort = (sort == null) ? Sort.by(Sort.Order.asc("dueDate")) : sort;
        return taskRepository.findByPriority(priority, finalSort);
    }

    public List<Task> getTaskByPriorityMultiple(List<Priority> priority, Sort sort) {
        Sort finalSort = (sort == null) ? Sort.by(Sort.Order.asc("priority"), Sort.Order.asc("dueDate")) : sort;
        return taskRepository.findByPriorityIn(priority, finalSort);
    }

    public List<Task> getTaskByCreatedAtBefore(LocalDateTime createdAt, Sort sort) {
        Sort finalSort = (sort == null) ? Sort.by(Sort.Order.asc("createdAt")) : sort;
        return taskRepository.findByCreatedAtBefore(createdAt, finalSort);
    }

    public List<Task> getTaskByCreatedAtAfter(LocalDateTime createdAt, Sort sort) {
        Sort finalSort = (sort == null) ? Sort.by(Sort.Order.asc("createdAt")) : sort;
        return taskRepository.findByCreatedAtAfter(createdAt, finalSort);
    }

    public List<Task> getTaskByCreatedAtBetween(LocalDateTime from, LocalDateTime to, Sort sort) {
        Sort finalSort = (sort == null) ? Sort.by(Sort.Order.asc("createdAt")) : sort;
        return taskRepository.findByCreatedAtBetween(from, to, finalSort);
    }

    public boolean isValidTask(int taskId) {
        List<Task> tasks = taskRepository.findByTaskId(taskId);
        return !tasks.isEmpty();
    }


    public boolean isWritePermissionGranted(int userIdMakingChanges, int taskId) {
        TaskGroup taskGroup = groupService.getGroupByUserIdAndTaskId(userIdMakingChanges, taskId);
        System.out.println(taskGroup.getPermission());
        return taskGroupService.isAllowedToWrite(taskGroup.getPermission(), taskId);
    }

    public boolean isDeletePermissionGranted(int userIdMakingChanges, int taskId) {
        TaskGroup taskGroup = groupService.getGroupByUserIdAndTaskId(userIdMakingChanges, taskId);
        return taskGroup != null && taskGroupService.isAllowedToDelete(taskGroup.getPermission(), taskId);
    }


}