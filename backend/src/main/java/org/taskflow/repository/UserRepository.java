package org.taskflow.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.taskflow.model.User;
import org.springframework.data.domain.Pageable;


import java.time.LocalDateTime;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Integer> {

    List<User> findAll(Sort sort);
    List<User> findByUserId(int userId);
    List<User> findByEmail(String email);
    List<User> findByEmailContaining(String email);
    List<User> findByUsername(String username);
    List<User> findByUsernameContaining(String username);
    List<User> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to, Sort sort);
    List<User> findByCreatedAtBefore(LocalDateTime localDateTime, Sort sort);
    List<User> findByCreatedAtAfter(LocalDateTime localDateTime, Sort sort);
    List<User> findByUpdatedAtBetween(LocalDateTime from, LocalDateTime to, Sort sort);
    List<User> findByUpdatedAtBefore(LocalDateTime localDateTime, Sort sort);
    List<User> findByUpdatedAtAfter(LocalDateTime localDateTime, Sort sort);

    Page<User> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);
    Page<User> findByCreatedAtBefore(LocalDateTime localDateTime, Pageable pageable);
    Page<User> findByCreatedAtAfter(LocalDateTime localDateTime, Pageable pageable);

    Page<User> findByUpdatedAtBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);
    Page<User> findByUpdatedAtBefore(LocalDateTime localDateTime, Pageable pageable);
    Page<User> findByUpdatedAtAfter(LocalDateTime localDateTime, Pageable pageable);

}
