package org.taskflow.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.taskflow.models.Group;
import org.taskflow.models.User;
import org.taskflow.models.UserGroup;

import java.time.LocalDateTime;
import java.util.List;

public interface UserGroupRepository extends JpaRepository <UserGroup, Integer> {
    List<UserGroup> findByUser (User user, Sort sort);
    List<UserGroup> findByUser (User user);
    List<UserGroup> findByGroup (Group group);
    List<UserGroup> findByUserAndGroup (User user, Group group);
    List<UserGroup> findByCreatedAtBetween (LocalDateTime from, LocalDateTime to);
    List<UserGroup> findByCreatedAtBefore (LocalDateTime before);
    List<UserGroup> findByCreatedAtAfter (LocalDateTime after);

}
