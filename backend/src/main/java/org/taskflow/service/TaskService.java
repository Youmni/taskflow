package org.taskflow.service;

import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.taskflow.DTO.TaskDTO;
import org.taskflow.model.*;
import org.taskflow.repository.TaskGroupRepository;
import org.taskflow.repository.TaskHistoryRepository;
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
import java.util.stream.Collectors;

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
    private EmailService emailService;
    @Autowired
    private TaskHistoryRepository taskHistoryRepository;

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

            LocalDate dueDate = LocalDate.of(taskRequest.getYear(), taskRequest.getMonth(), taskRequest.getDay());

            Task task = new Task(
                    taskRequest.getTitle(),
                    taskRequest.getDescription(),
                    taskRequest.getStatus(),
                    taskRequest.getPriority(),
                    dueDate,
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

            HashMap<String, Permission> emailWithPermissions = taskGroupService.getEmailsAndPermissionsByTaskId(task.getTaskId());

            emailWithPermissions.forEach((email, permission)->{
                try {
                    emailService.sendTaskCreatedEmail(task, email, permission);
                } catch (MessagingException e) {
                    throw new RuntimeException("The email could not send");
                }
            });

            return ResponseEntity.ok("Task created successfully");
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("There was an error processing your request: " + e.getMessage());
        }
    }

    public ResponseEntity<String> updateTask(int taskId, int userIdMakingChanges, String title, String description, Status status,Priority priority,LocalDate dueDate, String comment){
        System.out.println("1");
        if (!taskRepository.existsByTaskId(taskId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task not found");
        }
        System.out.println("2");

        if(!isWritePermissionGranted(userIdMakingChanges, taskId)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to update this task");
        }
        System.out.println("3");

        try {
            Task taskToUpdate = taskRepository.findByTaskId(taskId).getFirst();
            boolean isTaskChanged = false;
            System.out.println("5");

            if(title != null && !title.equals(taskToUpdate.getTitle())){
                taskToUpdate.setTitle(title);
                isTaskChanged = true;
            }
            if(description != null && !description.equals(taskToUpdate.getDescription())){
                taskToUpdate.setDescription(description);
                isTaskChanged = true;
            }
            if(status != null && !status.equals(taskToUpdate.getStatus())) {
                taskToUpdate.setStatus(status);
                isTaskChanged = true;
            }
            if(priority != null && !priority.equals(taskToUpdate.getPriority())) {
                taskToUpdate.setPriority(priority);
                isTaskChanged = true;
            }
            if(dueDate != null && !dueDate.equals(taskToUpdate.getDueDate())) {
                taskToUpdate.setDueDate(dueDate);
                isTaskChanged = true;
            }
            if(comment != null && !comment.equals(taskToUpdate.getComment())) {
                taskToUpdate.setComment(comment);
                isTaskChanged = true;
            }
            System.out.println("6");

            if(isTaskChanged) {
                System.out.println("7");

                taskRepository.save(taskToUpdate);
                taskHistoryService.createTaskHistory(taskToUpdate, userIdMakingChanges);
                return ResponseEntity.status(HttpStatus.OK).body("Task updated successfully");
            }else{
                return ResponseEntity.status(HttpStatus.OK).body("No changes detected");
            }
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR_MESSAGE + e.getMessage());
        }
    }



    public ResponseEntity<String> deleteTask(int taskId, int userId) {
        if(!isDeletePermissionGranted(userId, taskId)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to delete this task");
        }
        if (!taskRepository.existsByTaskId(taskId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task not found");
        }
        try{
            Task taskToDelete = getTaskById(taskId);

            List<Taskhistory> taskhistories = taskHistoryRepository.findByTask(taskToDelete);
            List<TaskGroup> taskGroups = taskGroupRepository.findByTask(taskToDelete);

            taskHistoryRepository.deleteAll(taskhistories);
            taskGroupRepository.deleteAll(taskGroups);

            taskRepository.deleteById(taskId);
            return ResponseEntity.ok("Task deleted successfully");
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR_MESSAGE + e.getMessage());
        }
    }

    public List<TaskDTO> getFilteredTasks(int userId,LocalDate dueDate,LocalDate dueDateAfter,LocalDate dueDateBefore, Priority priority, Status status){

        if(!userService.isValidUser(userId)){
           return new ArrayList<>();
        }
        User user = userService.getUserById(userId);
        List<Task> taskList = taskRepository.findByUser(user, Sort.by(Sort.Direction.DESC, "dueDate"));

        if(dueDate != null){
            taskList = taskList.stream()
                    .filter(task -> task.getDueDate().equals(dueDate))
                    .toList();
        }

        if(dueDateAfter != null){
            taskList = taskList.stream()
                    .filter(task -> task.getDueDate().isAfter(dueDateAfter))
                    .toList();
        }

        if(dueDateBefore != null){
            taskList = taskList.stream()
                    .filter(task -> task.getDueDate().isBefore(dueDateBefore))
                    .toList();
        }

        if(priority != null){
            taskList = taskList.stream()
                    .filter(task -> task.getPriority().equals(priority))
                    .toList();
        }

        if(status != null){
            taskList = taskList.stream()
                    .filter(task -> task.getStatus().equals(status))
                    .toList();
        }

        List<TaskDTO> taskDTOList = new ArrayList<>();

        for(Task task : taskList){
            TaskDTO taskDTO = new TaskDTO();

            taskDTO.setTaskId(task.getTaskId());
            taskDTO.setTitle(task.getTitle());
            taskDTO.setDescription(task.getDescription());
            taskDTO.setStatus(task.getStatus());
            taskDTO.setPriority(task.getPriority());
            taskDTO.setDueDate(task.getDueDate());
            taskDTO.setComment(task.getComment());
            taskDTO.setUserId(task.getUser().getUserId());

            HashMap<Integer, Permission> groups = new HashMap<>();

            List<TaskGroup> taskGroups = taskGroupRepository.findByTask(task);
            for(TaskGroup taskGroup : taskGroups){
                groups.put(taskGroup.getGroup().getGroupId(), taskGroup.getPermission());
            }
            taskDTO.setGroups(groups);
            taskDTOList.add(taskDTO);
        }
        return taskDTOList;
    }

    public List<TaskDTO> getFilteredSharedTasks(int userId,LocalDate dueDate,LocalDate dueDateAfter,LocalDate dueDateBefore, Priority priority, Status status){
        if(!userService.isValidUser(userId)){
            return new ArrayList<>();
        }

        List<Task> taskList = getAllSharedTasks(userId);

        if(dueDate != null){
            taskList = taskList.stream()
                    .filter(task -> task.getDueDate().equals(dueDate))
                    .toList();
        }

        if(dueDateAfter != null){
            taskList = taskList.stream()
                    .filter(task -> task.getDueDate().isAfter(dueDateAfter))
                    .toList();
        }

        if(dueDateBefore != null){
            taskList = taskList.stream()
                    .filter(task -> task.getDueDate().isBefore(dueDateBefore))
                    .toList();
        }

        if(priority != null){
            taskList = taskList.stream()
                    .filter(task -> task.getPriority().equals(priority))
                    .toList();
        }

        if(status != null){
            taskList = taskList.stream()
                    .filter(task -> task.getStatus().equals(status))
                    .toList();
        }

        List<TaskDTO> taskDTOList = new ArrayList<>();

        for(Task task : taskList){
            TaskDTO taskDTO = new TaskDTO();

            taskDTO.setTaskId(task.getTaskId());
            taskDTO.setTitle(task.getTitle());
            taskDTO.setDescription(task.getDescription());
            taskDTO.setStatus(task.getStatus());
            taskDTO.setPriority(task.getPriority());
            taskDTO.setDueDate(task.getDueDate());
            taskDTO.setComment(task.getComment());
            taskDTO.setUserId(task.getUser().getUserId());

            HashMap<Integer, Permission> groups = new HashMap<>();

            List<TaskGroup> taskGroups = taskGroupRepository.findByTask(task);
            for(TaskGroup taskGroup : taskGroups){
                groups.put(taskGroup.getGroup().getGroupId(), taskGroup.getPermission());
            }
            taskDTO.setGroups(groups);
            taskDTOList.add(taskDTO);
        }
        return taskDTOList;
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

    public List<Task> getAllSharedTasks(int userId) {
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


    public List<Task> getTaskByDueDateAndUserId(LocalDate dueDate, int userId) {
        if(!userService.isValidUser(userId)){
            return new ArrayList<>();
        }
        User user = userService.getUserById(userId);
        return taskRepository.findByDueDateAndUser(dueDate, user);
    }

    public List<Task> getTaskByDueDateBeforeAndUserId(LocalDate dueDate, int userId) {
        if(!userService.isValidUser(userId)){
            return new ArrayList<>();
        }
        User user = userService.getUserById(userId);
        return taskRepository.findByDueDateBeforeAndUser(dueDate, user, Sort.by(Sort.Direction.DESC, "dueDate"));
    }

    public List<Task> getTaskByDueDateAfterAndUserId(LocalDate dueDate, int userId) {
        if(!userService.isValidUser(userId)){
            return new ArrayList<>();
        }
        User user = userService.getUserById(userId);
        return taskRepository.findByDueDateAfterAndUser(dueDate, user, Sort.by(Sort.Direction.DESC, "dueDate"));
    }

    public List<Task> getTaskByUserid(int userId, Sort sort) {

        User user = userService.getUserById(userId);
        Sort finalSort = (sort == null) ? Sort.by(Sort.Order.asc("dueDate")) : sort;
        return taskRepository.findByUser(user, finalSort);
    }


    public List<Task> getTaskByPriorityAndUserId(Priority priority, int userId) {
        if(!userService.isValidUser(userId)){
            return new ArrayList<>();
        }
        User user = userService.getUserById(userId);
        return taskRepository.findByPriorityAndUser(priority, user, Sort.by(Sort.Order.asc("dueDate")));
    }


    public boolean isValidTask(int taskId) {
        List<Task> tasks = taskRepository.findByTaskId(taskId);
        return !tasks.isEmpty();
    }


    public boolean isWritePermissionGranted(int userIdMakingChanges, int taskId) {

        Task task = getTaskById(taskId);
        if(task.getUser().getUserId()==userIdMakingChanges){
            return true;
        }

        TaskGroup taskGroup = groupService.getGroupByUserIdAndTaskId(userIdMakingChanges, taskId);
        if(taskGroup == null) {
            return false;
        }
        return taskGroupService.isAllowedToWrite(taskGroup.getPermission(), taskId);
    }

    public boolean isDeletePermissionGranted(int userIdMakingChanges, int taskId) {
        Task task = getTaskById(taskId);
        if(task.getUser().getUserId()==userIdMakingChanges){
            return true;
        }
        TaskGroup taskGroup = groupService.getGroupByUserIdAndTaskId(userIdMakingChanges, taskId);
        return taskGroup != null && taskGroupService.isAllowedToDelete(taskGroup.getPermission(), taskId);
    }

    @Scheduled(cron = "0 52 20 * * ?", zone = "Europe/Brussels")
    public void scheduleReminderEmailsForTasks(){
        List<Task> allTasks = taskRepository.findAll();
        for(Task task : allTasks){
            if(task.getDueDate().equals(LocalDate.now().plusDays(1))){
                HashMap<String, Permission> emailWithPermissions = taskGroupService.getEmailsAndPermissionsByTaskId(task.getTaskId());

                emailWithPermissions.forEach((email, permission)->{
                    try {
                        emailService.sendTaskReminderEmail(task, email, permission);
                    } catch (MessagingException e) {
                        throw new RuntimeException("The reminder email could not send");
                    }
                });
            }
        }
    }
}