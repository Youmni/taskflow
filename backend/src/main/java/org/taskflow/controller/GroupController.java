package org.taskflow.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.taskflow.DTO.GroupWithUsersDTO;
import org.taskflow.model.Group;
import org.taskflow.model.UserGroup;
import org.taskflow.service.GroupService;
import org.taskflow.wrapper.UserGroupRequest;

import java.util.List;

@RestController
@RequestMapping("/group")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @CrossOrigin
    @PostMapping(value = "/createWithUsers/{ownerId}")
    public ResponseEntity<String> createGroupWithUsers(@RequestBody GroupWithUsersDTO groupDTO, @PathVariable int ownerId) {
        Group group = new Group(groupDTO.getGroupName(), groupDTO.getDescription());

        return groupService.createGroupWithUsers(group, groupDTO.getEmails(), ownerId);
    }

    @CrossOrigin
    @PostMapping(value = "/createWithoutUsers")
    public ResponseEntity<String> createGroupWithoutUsers(@RequestBody Group group) {
        return groupService.createGroup(group);
    }


    @CrossOrigin
    @PutMapping(value = "/add")
    public ResponseEntity<String> addUserToGroup(@RequestParam int groupId, @RequestParam String email) {
        return groupService.addUserToGroup(groupId, email);
    }

    @CrossOrigin
    @DeleteMapping(value = "/remove")
    public ResponseEntity<String> deleteUserFromGroup(@RequestParam int groupId, @RequestParam int userId) {
        return groupService.removeUserFromGroup(groupId, userId);
    }

    @CrossOrigin
    @GetMapping(value = "/ID")
    public List<UserGroup> getGroupsByUserId(@RequestParam int userId) {
        return groupService.getGroupsByUserId(userId);
    }
}
