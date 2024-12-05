package org.taskflow.controller;

import org.springframework.web.bind.annotation.*;
import org.taskflow.model.Taskhistory;
import org.taskflow.service.TaskHistoryService;
import org.taskflow.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/taskhistory")
public class TaskHistoryController {

    private final TaskHistoryService taskHistoryService;

    public TaskHistoryController(TaskHistoryService taskHistoryService) {
        this.taskHistoryService = taskHistoryService;
    }

    @CrossOrigin
    @GetMapping(value = "/get/{taskId}")
    public List<Taskhistory> getTaskHistories(@PathVariable int taskId) {
        return taskHistoryService.getTaskHistories(taskId);
    }
}
