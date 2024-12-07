package org.taskflow.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.taskflow.model.Group;
import org.taskflow.model.User;

import java.time.LocalDateTime;
import java.util.List;

public interface GroupRepository extends JpaRepository<Group, Integer> {

    List<Group> findByGroupId(int groupId);
    List<Group> findByGroupName(String groupName);
    List<Group> findByGroupNameContainingIgnoreCase(String groupName);
    List<Group> findByGroupNameStartingWith(String groupName);
//    List<Group> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
//    List<Group> findByCreatedAtBefore(LocalDateTime localDateTime);
//    List<Group> findByCreatedAtAfter(LocalDateTime localDateTime);
//    List<Group> findByUpdatedAtBetween(LocalDateTime from, LocalDateTime to);
//    List<Group> findByUpdatedAtBefore(LocalDateTime localDateTime);
//    List<Group> findByUpdatedAtAfter(LocalDateTime localDateTime);
    List<Group> findByCreatedBy(User user, Sort sort);
}
