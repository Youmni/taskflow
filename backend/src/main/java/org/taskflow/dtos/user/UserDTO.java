package org.taskflow.dtos.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.taskflow.enums.UserType;

@Data
@AllArgsConstructor
public class UserDTO {
    private String username;
    private String email;
    private UserType role;
    private String password;
}
