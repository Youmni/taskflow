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
    List<UserGroup> findByCreatedAtBetween (LocalDateTime from, LocalDateTime to, Sort sort);
    List<UserGroup> findByCreatedAtBefore (LocalDateTime before, Sort sort);
    List<UserGroup> findByCreatedAtAfter (LocalDateTime after, Sort sort);

    Page<UserGroup> findByUser (User user, Pageable pageable);
    Page<UserGroup> findByGroup (Group group, Pageable pageable);
    Page<UserGroup> findByCreatedAtBetween (LocalDateTime from, LocalDateTime to, Pageable pageable);
    Page<UserGroup> findByCreatedAtBefore (LocalDateTime before, Pageable pageable);
    Page<UserGroup> findByCreatedAtAfter (LocalDateTime after, Pageable pageable);
}
