package org.taskflow.controller;

import org.springframework.web.bind.annotation.*;
import org.taskflow.DTO.TaskHistoryDTO;
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
    @GetMapping(value = "/{taskId}/ID")
    public List<TaskHistoryDTO> getTaskHistories(@PathVariable int taskId, @RequestParam int userId) {
        return taskHistoryService.getTaskHistories(taskId, userId);
    }
}
