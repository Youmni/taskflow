package org.taskflow.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.taskflow.model.Task;
import org.taskflow.model.TaskHistoryKey;
import org.taskflow.model.Taskhistory;
import org.taskflow.model.User;
import org.taskflow.repository.TaskHistoryRepository;
import org.taskflow.repository.TaskRepository;
import org.taskflow.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class TaskHistoryService {

    private TaskRepository taskRepository;
    private TaskService taskService;
    private UserRepository userRepository;
    private TaskHistoryRepository taskHistoryRepository;

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

    public List<Taskhistory> getTaskHistories(int taskId) {
        List<Task> tasks = taskRepository.findByTaskId(taskId);
        if(tasks.isEmpty()){
            return new ArrayList<>();
        }
        Sort sort = Sort.by(Sort.Order.desc("createdAt"));
        return taskHistoryRepository.findByTask(tasks.getFirst(),sort);
    }

    public int generateHistoryId(int taskId){
        List<Taskhistory> taskhistories = getTaskHistories(taskId);
        return taskhistories.size()+1;
    }

}