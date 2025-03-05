package org.taskflow.controllers;

import org.springframework.web.bind.annotation.*;
import org.taskflow.dtos.TaskHistoryDTO;
import org.taskflow.service.TaskHistoryService;

import java.util.List;

@RestController
@RequestMapping("/taskhistory")
public class TaskHistoryController {

    private final TaskHistoryService taskHistoryService;

    public TaskHistoryController(TaskHistoryService taskHistoryService) {
        this.taskHistoryService = taskHistoryService;
    }

    @CrossOrigin
    @GetMapping(value = "/{taskId}/ID")
    public List<TaskHistoryDTO> getTaskHistories(@PathVariable int taskId, @RequestParam int userId) {
        return taskHistoryService.getTaskHistories(taskId, userId);
    }
}
