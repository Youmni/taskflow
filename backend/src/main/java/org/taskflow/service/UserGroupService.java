package org.taskflow.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.taskflow.model.Group;
import org.taskflow.model.User;
import org.taskflow.model.UserGroup;
import org.taskflow.model.UserGroupKey;
import org.taskflow.repository.UserGroupRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UserGroupService {

    private final UserService userService;
    private final GroupService groupService;
    private final UserGroupRepository userGroupRepository;

    @Autowired
    public UserGroupService(UserService userService, GroupService groupService, UserGroupRepository userGroupRepository) {
        this.userService = userService;
        this.groupService = groupService;
        this.userGroupRepository = userGroupRepository;
    }

    public void addUserGroup(int groupId, int userId) {
        if(userService.isValidUser(userId) && groupService.isValidGroup(groupId)) {
            try{
                User user = userService.getUserById(userId);
                Group group = groupService.getGroupById(groupId);

                if (user == null || group == null) {
                    throw new Exception("User or Group not found");
                }

                UserGroup userGroup = new UserGroup(user, group);
                userGroupRepository.save(userGroup);
            }catch(Exception e){
                System.err.println(e.getMessage());
            }
        }
    }

    public boolean isUserInGroup(int groupId, int userId) throws Exception{
        if (!userService.isValidUser(userId)) {
            throw new Exception("Invalid user");
        }
        if (!groupService.isValidGroup(groupId)) {
            throw new Exception("Invalid group");
        }
        User user = userService.getUserById(userId);
        Group group = groupService.getGroupById(groupId);

        if (user== null || group == null) {
            throw new Exception("User or Group not found");
        }
        List<UserGroup> userGroups = userGroupRepository.findByUserAndGroup(user, group);
        return !userGroups.isEmpty();
    }
}
