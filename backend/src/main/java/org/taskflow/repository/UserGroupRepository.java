package org.taskflow.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.taskflow.model.Group;
import org.taskflow.model.User;
import org.taskflow.model.UserGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface UserGroupRepository extends JpaRepository <UserGroup, Integer> {
    List<UserGroup> findByUser (User user, Sort sort);
    List<UserGroup> findByGroup (Group group);
    List<UserGroup> findByUserAndGroup (User user, Group group);
    List<UserGroup> findByCreatedAtBetween (LocalDateTime from, LocalDateTime to);
    List<UserGroup> findByCreatedAtBefore (LocalDateTime before);
    List<UserGroup> findByCreatedAtAfter (LocalDateTime after);

}
