package org.taskflow.dtos.task;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.taskflow.enums.Permission;

import java.util.HashMap;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskCreationDTO {
    private TaskDTO taskDTO;
    private HashMap<Integer, Permission> group;
}
