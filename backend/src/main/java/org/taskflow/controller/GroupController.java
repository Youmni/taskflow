package org.taskflow.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.taskflow.DTO.GroupDTO;
import org.taskflow.DTO.GroupRequestDTO;
import org.taskflow.DTO.GroupWithUsersDTO;
import org.taskflow.model.Group;
import org.taskflow.model.User;
import org.taskflow.model.UserGroup;
import org.taskflow.service.GroupService;
import org.taskflow.service.UserService;
import org.taskflow.wrapper.UserGroupRequest;

import java.util.List;

@RestController
@RequestMapping("/group")
public class GroupController {

    private final GroupService groupService;
    private final UserService userService;

    public GroupController(GroupService groupService, UserService userService) {
        this.groupService = groupService;
        this.userService = userService;
    }

    @CrossOrigin
    @PostMapping(value = "/createWithUsers/{ownerId}")
    public ResponseEntity<String> createGroupWithUsers(@RequestBody GroupWithUsersDTO groupDTO, @PathVariable int ownerId) {
        if(!userService.isValidUser(ownerId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid ownerId");
        }
        User user = userService.getUserById(ownerId);
        Group group = new Group(groupDTO.getGroupName(), groupDTO.getDescription(), user);

        return groupService.createGroupWithUsers(group, groupDTO.getEmails(), ownerId);
    }

    @CrossOrigin
    @PostMapping(value = "/createWithoutUsers/{ownerId}")
    public ResponseEntity<String> createGroupWithoutUsers(@RequestBody GroupDTO group, @PathVariable int ownerId) {
        return groupService.createGroup(group,ownerId);
    }

    @CrossOrigin
    @DeleteMapping(value = "/{groupId}/remove")
    public ResponseEntity<String> deleteUserFromGroup(@PathVariable int groupId, @RequestParam int ownerId) {
        return groupService.deleteGroup(groupId, ownerId);
    }

    @CrossOrigin
    @PutMapping(value = "/{groupId}/add-user")
    public ResponseEntity<String> addUserToGroup(@PathVariable int groupId, @RequestParam String email, @RequestParam int ownerId) {
        return groupService.addUserToGroup(groupId, email, ownerId);
    }

    @CrossOrigin
    @PutMapping(value = "/{groupId}/add-users")
    public ResponseEntity<String> addUsersToGroup(@PathVariable int groupId, @RequestBody List<String> emails, @RequestParam int ownerId) {
        return groupService.addUsersToGroup(groupId, emails, ownerId);
    }

    @CrossOrigin
    @PutMapping(value = "/{groupId}/remove-users")
    public ResponseEntity<String> deleteUserFromGroup(@PathVariable int groupId, @RequestBody List<String> emails, @RequestParam int ownerId) {
        return groupService.removeUsersFromGroup(groupId, emails, ownerId);
    }

    @CrossOrigin
    @GetMapping(value = "/{userId}")
    public List<GroupRequestDTO> getGroupsByUserId(@PathVariable int userId) {
        return groupService.getGroupsByUserId(userId);
    }
}
