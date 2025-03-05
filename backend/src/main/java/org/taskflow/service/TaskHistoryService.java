package org.taskflow.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.taskflow.dtos.TaskHistoryDTO;
import org.taskflow.enums.Permission;
import org.taskflow.models.*;
import org.taskflow.repository.TaskGroupRepository;
import org.taskflow.repository.TaskHistoryRepository;
import org.taskflow.repository.TaskRepository;
import org.taskflow.repository.UserRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class TaskHistoryService {

    private TaskRepository taskRepository;
    private TaskService taskService;
    private UserRepository userRepository;
    private TaskHistoryRepository taskHistoryRepository;
    private TaskGroupRepository taskGroupRepository;

    @Autowired
    public void setTaskRepository(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Autowired
    @Lazy
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    public void setTaskHistoryRepository(TaskHistoryRepository taskHistoryRepository) {
        this.taskHistoryRepository = taskHistoryRepository;
    }


    public ResponseEntity<String> createTaskHistory(Task task, int userId) {
        if(!taskRepository.existsByTaskId(task.getTaskId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No task found. Task history cannot be created");
        }
        try{
            if (!userRepository.existsByUserId(userId)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("User not found. User history cannot be created");
            }
            List<User> users = userRepository.findByUserId(userId);
            List<Task> originalTasks = taskRepository.findByTaskId(task.getTaskId());

            if (users.isEmpty() || originalTasks.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Task or User not found. Task history cannot be created");
            }

            Taskhistory taskhistory = new Taskhistory(
                    task.getTitle(),
                    task.getDescription(),
                    task.getStatus(),
                    task.getPriority(),
                    task.getDueDate(),
                    task.getComment(),
                    users.getFirst(),
                    task
            );
            int historyId = generateHistoryId(task.getTaskId());
            TaskHistoryKey taskHistoryKey = new TaskHistoryKey(task.getTaskId(), historyId);
            taskhistory.setId(taskHistoryKey);

            taskHistoryRepository.save(taskhistory);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Task history created");
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("There was an error processing your request: " + task.getTaskId());
        }
    }

    public ResponseEntity<String> deleteTaskHistories(int taskId, int userId) {
        if(!taskService.isDeletePermissionGranted(userId, taskId)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You do not have permission to delete history");
        }
        if(!taskRepository.existsByTaskId(taskId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No task found");
        }
        try{
            List<Task> tasks = taskRepository.findByTaskId(taskId);

            Task originalTask = tasks.getFirst();
            List<Taskhistory> taskHistories = taskHistoryRepository.findByTask(originalTask);

            taskHistoryRepository.deleteAll(taskHistories);

            return ResponseEntity.ok()
                    .body("Task histories deleted");
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("There was an error processing your request: " + e.getMessage());
        }
    }

    public ResponseEntity<String> deleteTaskIndividualHistory(int taskId, int  taskHistoryId, int userId) {
        if(!taskService.isDeletePermissionGranted(userId, taskId)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You do not have permission to delete history");
        }

        TaskHistoryKey taskHistoryKey = new TaskHistoryKey(taskId, taskHistoryId);
        List<Taskhistory> taskhistoryList = taskHistoryRepository.findById(taskHistoryKey);
        if(taskhistoryList.isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No task history found");
        }
        try{
            Taskhistory taskhistory = taskhistoryList.getFirst();
            taskHistoryRepository.delete(taskhistory);
            return ResponseEntity.ok()
                    .body("Individual task history deleted");
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("There was an error processing your request: " + e.getMessage());
        }
    }

    public List<Taskhistory> getIndividualTaskHistory(int taskId, int taskHistoryId) {
        TaskHistoryKey taskHistoryKey = new TaskHistoryKey(taskId, taskHistoryId);
        return taskHistoryRepository.findById(taskHistoryKey);
    }

    public List<TaskHistoryDTO> getTaskHistories(int taskId, int ownerId) {
        List<Task> tasks = taskRepository.findByTaskId(taskId);
        if(tasks.isEmpty() || !taskService.isValidTask(taskId)){
            return new ArrayList<>();
        }

        Task task = tasks.getFirst();
        if(!(task.getUser().getUserId()==ownerId)){
            return new ArrayList<>();
        }


        List<TaskHistoryDTO> taskHistoryList = new ArrayList<>();
        List<Taskhistory> taskhistories = taskHistoryRepository.findByTask(task,Sort.by(Sort.Order.asc("createdAt")));

        for(Taskhistory taskhistory : taskhistories){
            TaskHistoryDTO taskHistoryDTO = new TaskHistoryDTO();
            taskHistoryDTO.setTaskId(taskhistory.getTask().getTaskId());
            taskHistoryDTO.setHistoryId(taskhistory.getId().getHistoryId());
            taskHistoryDTO.setTitle(taskhistory.getTitle());
            taskHistoryDTO.setDescription(taskhistory.getDescription());
            taskHistoryDTO.setStatus(taskhistory.getStatus());
            taskHistoryDTO.setPriority(taskhistory.getPriority());
            taskHistoryDTO.setDueDate(taskhistory.getDueDate());
            taskHistoryDTO.setComment(taskhistory.getComment());
            taskHistoryDTO.setUserId(taskhistory.getUser().getUserId());

            HashMap<Integer, Permission> groups = new HashMap<>();

            List<TaskGroup> taskGroups = taskGroupRepository.findByTask(taskhistory.getTask());
            for(TaskGroup taskGroup : taskGroups){
                groups.put(taskGroup.getGroup().getGroupId(), taskGroup.getPermission());
            }
            taskHistoryDTO.setGroups(groups);
            taskHistoryList.add(taskHistoryDTO);
        }
        return taskHistoryList;
    }

    public int generateHistoryId(int taskId){
        Task task = taskService.getTaskById(taskId);
        List<Taskhistory> taskhistories = taskHistoryRepository.findByTask(task);
        return taskhistories.size()+1;
    }

    @Autowired
    public void setTaskGroupRepository(TaskGroupRepository taskGroupRepository) {
        this.taskGroupRepository = taskGroupRepository;
    }
}