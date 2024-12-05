package org.taskflow.controller;

import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.taskflow.model.*;
import org.taskflow.repository.UserRepository;
import org.taskflow.service.TaskService;
import org.taskflow.service.UserService;
import org.taskflow.wrapper.TaskCreationRequest;
import org.taskflow.wrapper.TaskRequest;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/task")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @CrossOrigin
    @PostMapping(value = "/create")
    public ResponseEntity<String> createTask(@RequestBody TaskCreationRequest taskCreationRequest) {
        return taskService.createTask(taskCreationRequest);
    }

    @CrossOrigin
    @PutMapping(value = "/update")
    public ResponseEntity<String> updateTask(@RequestParam int taskId, @RequestParam int userIdMakingChanges, @RequestBody Task task) {
        return taskService.updateTask(taskId, task, userIdMakingChanges);
    }

    @CrossOrigin
    @DeleteMapping(value = "/delete")
    public ResponseEntity<String> deleteTask(@RequestParam int taskId, @RequestParam int userId) {
        return taskService.deleteTask(taskId, userId);
    }

    @CrossOrigin
    @GetMapping(value = "/{userId}")
    public List<Task> getTaskByUserid(@PathVariable int userId) {
        return taskService.getTaskByUserid(userId, Sort.by(Sort.Direction.DESC, "dueDate"));
    }

    @CrossOrigin
    @GetMapping(value = "/shared/{userId}/due-date")
    public List<Task> getTasksForUserByDueDate(@PathVariable int userId, @RequestParam LocalDate dueDate) {
        return taskService.getTasksForUserByDueDate(userId, dueDate);
    }

    @CrossOrigin
    @GetMapping(value = "/shared/{userId}/due-date/after")
    public List<Task> getTasksForUserByAfterDueDate(@PathVariable int userId, @RequestParam LocalDate dueDate) {
        return taskService.getTasksForUserByAfterDueDate(userId, dueDate);
    }

    @CrossOrigin
    @GetMapping(value = "/shared/{userId}/duedate/before")
    public List<Task> getTasksForUserByBeforeDueDate(@PathVariable int userId, @RequestParam LocalDate dueDate) {
        return taskService.getTasksForUserByBeforeDueDate(userId, dueDate);
    }

    @CrossOrigin
    @GetMapping(value = "/{userId}/due-date")
    public List<Task> getTasksByUserForDueDate(@PathVariable int userId, @RequestParam LocalDate dueDate) {
        return taskService.getTaskByDueDateAndUserId(dueDate, userId);
    }


    @CrossOrigin
    @GetMapping(value = "/{userId}/due-date/after")
    public List<Task> getTasksByUserForDueDateAfter(@PathVariable int userId, @RequestParam LocalDate dueDate) {
        return taskService.getTaskByDueDateAfterAndUserId(dueDate, userId);
    }

    @CrossOrigin
    @GetMapping(value = "/{userId}/duedate/before")
    public List<Task> getTasksByUserForDueDateBefore(@PathVariable int userId, @RequestParam LocalDate dueDate) {
        return taskService.getTaskByDueDateBeforeAndUserId(dueDate,userId);
    }


    @CrossOrigin
    @GetMapping(value = "/shared/{userId}/priority")
    public List<Task> getTasksForUsersByPriority(@PathVariable int userId, @RequestParam Priority priority) {
        return taskService.getTasksForUsersByPriority(userId, priority);
    }

    @CrossOrigin
    @GetMapping(value = "/{userId}/priority")
    public List<Task> getTasksByUserForPriority(@PathVariable int userId, @RequestParam Priority priority) {
        return taskService.getTaskByPriorityAndUserId(priority, userId);
    }

    @CrossOrigin
    @GetMapping(value = "/shared/{userId}/status")
    public List<Task> getTasksForUsersByStatus(@PathVariable int userId, @RequestParam Status status) {
        return taskService.getTasksForUsersByStatus(userId, status);
    }

    @CrossOrigin
    @GetMapping(value = "/{userId}/status")
    public List<Task> getTasksByUsersForStatus(@PathVariable int userId, @RequestParam Status status) {
        return taskService.getTasksForUsersByStatus(userId, status);
    }
}
