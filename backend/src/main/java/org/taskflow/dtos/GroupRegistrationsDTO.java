package org.taskflow.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupRegistrationsDTO {
    private int groupId;
    private String groupName;
    private String description;
    private List<String> emails;
}