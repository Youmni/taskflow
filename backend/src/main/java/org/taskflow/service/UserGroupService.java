package org.taskflow.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.taskflow.model.Group;
import org.taskflow.model.User;
import org.taskflow.model.UserGroup;
import org.taskflow.repository.UserGroupRepository;

import java.util.List;

@Service
public class UserGroupService {

    private UserDataService userDataService;
    private GroupService groupService;
    private UserGroupRepository userGroupRepository;

    @Autowired
    public void setUserDataService(UserDataService userDataService) {
        this.userDataService = userDataService;
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
        if(userDataService.isValidUser(userId) && groupService.isValidGroup(groupId)) {
            try{
                User user = userDataService.getUserById(userId);
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
        if (!userDataService.isValidUser(userId)) {
            throw new Exception("Invalid user");
        }
        if (!groupService.isValidGroup(groupId)) {
            throw new Exception("Invalid group");
        }
        User user = userDataService.getUserById(userId);
        Group group = groupService.getGroupById(groupId);

        if (user== null || group == null) {
            throw new Exception("User or Group not found");
        }
        List<UserGroup> userGroups = userGroupRepository.findByUserAndGroup(user, group);
        return !userGroups.isEmpty();
    }
}