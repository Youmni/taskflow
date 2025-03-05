package org.taskflow.dtos.task;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.taskflow.enums.Permission;
import org.taskflow.enums.Priority;
import org.taskflow.enums.Status;

import java.time.LocalDate;
import java.util.HashMap;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskHistoryDTO {
    private int historyId;
    private String title;
    private String description;
    private Status status;
    private Priority priority;
    private LocalDate dueDate;
    private String comment;
    private int userId;
    private int taskId;
    private HashMap<Integer, Permission> groups;
}
