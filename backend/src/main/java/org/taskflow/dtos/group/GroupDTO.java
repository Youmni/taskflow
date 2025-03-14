package org.taskflow.dtos.group;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GroupDTO {
    private String groupName;
    private String description;
}