package org.taskflow.dtos.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.taskflow.models.Group;
import java.util.List;

@Data
@AllArgsConstructor
public class UserGroupDTO {
    private Group group;
    private List<String> emails;
}
