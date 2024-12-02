package org.taskflow.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.taskflow.model.Priority;
import org.taskflow.model.Status;
import org.taskflow.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.taskflow.model.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Integer> {

    List<Task> findByTaskId(int taskId);
    List<Task> findByTitle(String name);
    List<Task> findByTitleContainingIgnoreCase(String title);
    List<Task> findByDueDate(LocalDate dueDate, Sort sort);
    List<Task> findByDueDateBefore(LocalDate dueDate, Sort sort);
    List<Task> findByDueDateAfter(LocalDate dueDate, Sort sort);
    List<Task> findByDueDateBetween(LocalDate from, LocalDate to, Sort sort);
    List<Task> findByStatus(Status status, Sort sort);
    List<Task> findByStatusIn(List<Status> statuses, Sort sort);
    List<Task> findByPriority(Priority priority, Sort sort);
    List<Task> findByPriorityIn(List<Priority> priorities, Sort sort);
    List<Task> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to, Sort sort);
    List<Task> findByCreatedAtAfter(LocalDateTime createdAt, Sort sort);
    List<Task> findByCreatedAtBefore(LocalDateTime createdAt, Sort sort);
    List<Task> findByUser (User user, Sort sort);

    Page<Task> findByUser (User user, Pageable pageable);
    Page<Task> findByDueDate(LocalDate dueDate, Pageable pageable);
    Page<Task> findByDueDateBefore(LocalDate dueDate, Pageable pageable);
    Page<Task> findByDueDateAfter(LocalDate dueDate, Pageable pageable);
    Page<Task> findByStatus(Status status, Pageable pageable);
    Page<Task> findByStatusIn(List<Status> statuses, Pageable pageable);
    Page<Task> findByPriority(Priority priority, Pageable pageable);
    Page<Task> findByPriorityIn(List<Priority> priorities, Pageable pageable);
    Page<Task> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to, Pageable pageable);
    Page<Task> findByCreatedAtBefore(LocalDateTime createdAt, Pageable pageable);
    Page<Task> findByCreatedAtAfter(LocalDateTime createdAt, Pageable pageable);

    boolean existsByTaskId(int taskId);
    boolean existsByTitle(String title);

}
