package org.taskflow.service;

import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.taskflow.dtos.TaskDTO;
import org.taskflow.enums.Permission;
import org.taskflow.enums.Priority;
import org.taskflow.enums.Status;
import org.taskflow.models.*;
import org.taskflow.repository.TaskGroupRepository;
import org.taskflow.repository.TaskHistoryRepository;
import org.taskflow.repository.TaskRepository;
import org.taskflow.repository.UserGroupRepository;
import org.taskflow.dtos.TaskCreationDTO;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
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

    public ResponseEntity<String> createTask(TaskCreationDTO taskCreationDTO) {
        try {
            TaskDTO taskRequest = taskCreationDTO.getTaskDTO();
            HashMap<Integer, Permission> groups = taskCreationDTO.getGroup();

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

    public ResponseEntity<String> updateTask(int taskId, int userIdMakingChanges, String title, String description, Status status, Priority priority, LocalDate dueDate, String comment) {
        if (!taskRepository.existsByTaskId(taskId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task not found");
        }

        if (!isWritePermissionGranted(userIdMakingChanges, taskId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to update this task");
        }

        try {
            Task taskToUpdate = taskRepository.findByTaskId(taskId).getFirst();
            boolean isTaskChanged = false;

            isTaskChanged = updateTaskField(title, taskToUpdate::getTitle, taskToUpdate::setTitle) || isTaskChanged;
            isTaskChanged = updateTaskField(description, taskToUpdate::getDescription, taskToUpdate::setDescription) || isTaskChanged;
            isTaskChanged = updateTaskField(status, taskToUpdate::getStatus, taskToUpdate::setStatus) || isTaskChanged;
            isTaskChanged = updateTaskField(priority, taskToUpdate::getPriority, taskToUpdate::setPriority) || isTaskChanged;
            isTaskChanged = updateTaskField(dueDate, taskToUpdate::getDueDate, taskToUpdate::setDueDate) || isTaskChanged;
            isTaskChanged = updateTaskField(comment, taskToUpdate::getComment, taskToUpdate::setComment) || isTaskChanged;

            if (isTaskChanged) {
                taskRepository.save(taskToUpdate);
                taskHistoryService.createTaskHistory(taskToUpdate, userIdMakingChanges);
                return ResponseEntity.status(HttpStatus.OK).body("Task updated successfully");
            } else {
                return ResponseEntity.status(HttpStatus.OK).body("No changes detected");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR_MESSAGE + e.getMessage());
        }
    }

    private <T> boolean updateTaskField(T newValue, Supplier<T> getter, Consumer<T> setter) {
        if (newValue != null && !newValue.equals(getter.get())) {
            setter.accept(newValue);
            return true;
        }
        return false;
    }



    public ResponseEntity<String> deleteTask(int taskId, int userId) {
        if (!taskRepository.existsByTaskId(taskId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task not found");
        }
        if(!isDeletePermissionGranted(userId, taskId)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to delete this task");
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

    public List<TaskDTO> getFilteredTasks(int userId, LocalDate dueDate, LocalDate dueDateAfter, LocalDate dueDateBefore, Priority priority, Status status) {
        if (!userService.isValidUser(userId)) {
            return Collections.emptyList();
        }
        Optional<User> userOpt = Optional.ofNullable(userService.getUserById(userId));
        if (userOpt.isEmpty()) {
            return Collections.emptyList();
        }
        User user = userOpt.get();

        List<Task> taskList = taskRepository.findByUser(user, Sort.by(Sort.Direction.DESC, "dueDate"));

        taskList = taskList.stream()
                .filter(task -> applyFilters(task, dueDate, dueDateAfter, dueDateBefore, priority, status))
                .toList();

        return taskList.stream()
                .map(this::convertToTaskDTO)
                .collect(Collectors.toList());
    }

    private boolean applyFilters(Task task, LocalDate dueDate, LocalDate dueDateAfter, LocalDate dueDateBefore, Priority priority, Status status) {
        boolean matches = true;

        if (dueDate != null && !task.getDueDate().equals(dueDate)) {
            matches = false;
        }
        if (dueDateAfter != null && !task.getDueDate().isAfter(dueDateAfter)) {
            matches = false;
        }
        if (dueDateBefore != null && !task.getDueDate().isBefore(dueDateBefore)) {
            matches = false;
        }
        if (priority != null && !task.getPriority().equals(priority)) {
            matches = false;
        }
        if (status != null && !task.getStatus().equals(status)) {
            matches = false;
        }

        return matches;
    }

    private TaskDTO convertToTaskDTO(Task task) {
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setTaskId(task.getTaskId());
        taskDTO.setTitle(task.getTitle());
        taskDTO.setDescription(task.getDescription());
        taskDTO.setStatus(task.getStatus());
        taskDTO.setPriority(task.getPriority());
        taskDTO.setDueDate(task.getDueDate());
        taskDTO.setComment(task.getComment());
        taskDTO.setUserId(task.getUser().getUserId());

        // Zet de groepen om in een HashMap
        HashMap<Integer, Permission> groups = new HashMap<>();
        List<TaskGroup> taskGroups = taskGroupRepository.findByTask(task);
        taskGroups.forEach(taskGroup -> groups.put(taskGroup.getGroup().getGroupId(), taskGroup.getPermission()));

        taskDTO.setGroups(groups);
        return taskDTO;
    }

    public List<TaskDTO> getFilteredSharedTasks(int userId, LocalDate dueDate, LocalDate dueDateAfter, LocalDate dueDateBefore, Priority priority, Status status) {
        if (!userService.isValidUser(userId)) {
            return Collections.emptyList();
        }
        List<Task> taskList = getAllSharedTasks(userId);

        taskList = taskList.stream()
                .filter(task -> applyFilters(task, dueDate, dueDateAfter, dueDateBefore, priority, status))
                .toList();

        return taskList.stream()
                .map(this::convertToTaskDTO)
                .collect(Collectors.toList());
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