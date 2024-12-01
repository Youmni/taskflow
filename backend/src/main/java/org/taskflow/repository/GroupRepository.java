package org.taskflow.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.taskflow.model.Group;

import java.time.LocalDateTime;
import java.util.List;

public interface GroupRepository extends JpaRepository<Group, Integer> {

    List<Group> findByGroupId(int groupId);
    List<Group> findByGroupName(String groupName);
    List<Group> findByGroupNameContaining(String groupName);
    List<Group> findByGroupNameStartingWith(String groupName);
    List<Group> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
    List<Group> findByCreatedAtBefore(LocalDateTime localDateTime);
    List<Group> findByCreatedAtAfter(LocalDateTime localDateTime);
    List<Group> findByUpdatedAtBetween(LocalDateTime from, LocalDateTime to);
    List<Group> findByUpdatedAtBefore(LocalDateTime localDateTime);
    List<Group> findByUpdatedAtAfter(LocalDateTime localDateTime);

    Page<Group> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);
    Page<Group> findByCreatedAtBefore(LocalDateTime localDateTime, Pageable pageable);
    Page<Group> findByCreatedAtAfter(LocalDateTime localDateTime, Pageable pageable);
    Page<Group> findByUpdatedAtBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);
    Page<Group> findByUpdatedAtBefore(LocalDateTime localDateTime, Pageable pageable);
    Page<Group> findByUpdatedAtAfter(LocalDateTime localDateTime, Pageable pageable);

}