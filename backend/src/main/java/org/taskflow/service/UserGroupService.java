package org.taskflow.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.taskflow.models.Group;
import org.taskflow.models.User;
import org.taskflow.models.UserGroup;
import org.taskflow.models.UserGroupKey;
import org.taskflow.repository.UserGroupRepository;

import java.util.ArrayList;
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

    public List<String> getEmailsByGroupId(int groupId) {

        if(!groupService.isValidGroup(groupId)) {
            return new ArrayList<>();
        }
        Group group = groupService.getGroupById(groupId);
        List<UserGroup> userGroups = userGroupRepository.findByGroup(group);
        List<String> emails = new ArrayList<>();

        for(UserGroup userGroup : userGroups) {
            emails.add(userGroup.getUser().getEmail());
        }
        return emails;
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

    public boolean isUserInGroup(int groupId, String email) throws Exception{
        if (!userService.isValidUser(email)) {
            throw new Exception("Invalid user");
        }
        if (!groupService.isValidGroup(groupId)) {
            throw new Exception("Invalid group");
        }
        User user = userService.getUserByEmail(email);
        Group group = groupService.getGroupById(groupId);

        if (user== null || group == null) {
            throw new Exception("User or Group not found");
        }
        List<UserGroup> userGroups = userGroupRepository.findByUserAndGroup(user, group);
        return !userGroups.isEmpty();
    }
}