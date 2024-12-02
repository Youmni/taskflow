package org.taskflow.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.taskflow.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface TaskHistoryRepository extends JpaRepository<Taskhistory, Integer> {

    List<Taskhistory> findById(TaskHistoryKey historyId);
    List<Taskhistory> findByTitle(String name);
    List<Taskhistory> findByTitleContainingIgnoreCase(String title);
    List<Taskhistory> findByDueDate(LocalDate dueDate, Sort sort);
    List<Taskhistory> findByDueDateBefore(LocalDate dueDate, Sort sort);
    List<Taskhistory> findByDueDateAfter(LocalDate dueDate, Sort sort);
    List<Taskhistory> findByDueDateBetween(LocalDate from, LocalDate to, Sort sort);
    List<Taskhistory> findByStatus(Status status, Sort sort);
    List<Taskhistory> findByStatusIn(List<Status> statuses, Sort sort);
    List<Taskhistory> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to, Sort sort);
    List<Taskhistory> findByCreatedAtAfter(LocalDateTime createdAt, Sort sort);
    List<Taskhistory> findByCreatedAtBefore(LocalDateTime createdAt, Sort sort);
    List<Taskhistory> findByPriority(Priority priority, Sort sort);
    List<Taskhistory> findByPriorityIn(List<Priority> priorities, Sort sort);
    List<Taskhistory> findByTask(Task task, Sort sort);
    List<Taskhistory> findByTask(Task task);


    Page<Taskhistory> findByTask(Task task, Pageable pageable);

}
