package org.taskflow.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.taskflow.model.Group;
import org.taskflow.model.User;
import org.taskflow.model.UserGroup;
import org.taskflow.model.UserGroupKey;
import org.taskflow.repository.UserGroupRepository;

import java.util.List;

@Service
public class UserGroupService {

    private UserService userService;
    private GroupService groupService;
    private UserGroupRepository userGroupRepository;

    @Autowired
    public void setUserDataService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    @Lazy
    public void setGroupService(GroupService groupService) {
        this.groupService = groupService;
    }

    @Autowired
    public void setUserGroupRepository(UserGroupRepository userGroupRepository) {
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

                UserGroupKey userGroupKey = new UserGroupKey(userId, groupId);
                UserGroup userGroup = new UserGroup(user, group);
                userGroup.setId(userGroupKey);

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