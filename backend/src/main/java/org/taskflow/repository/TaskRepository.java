package org.taskflow.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.taskflow.enums.Priority;
import org.taskflow.enums.Status;
import org.taskflow.models.Task;
import org.taskflow.models.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Integer> {

    List<Task> findByTaskId(int taskId);
    List<Task> findByTitle(String name);
    List<Task> findByTitleAndUser(String name, User user);
    List<Task> findByTitleContainingIgnoreCase(String title);

    List<Task> findByDueDate(LocalDate dueDate, Sort sort);
    List<Task> findByDueDateAndUser(LocalDate dueDate,User user);
    List<Task> findByDueDateBefore(LocalDate dueDate, Sort sort);
    List<Task> findByDueDateBeforeAndUser(LocalDate dueDate, User user, Sort sort);
    List<Task> findByDueDateAfter(LocalDate dueDate, Sort sort);
    List<Task> findByDueDateAfterAndUser(LocalDate dueDate, User user, Sort sort);
    List<Task> findByDueDateBetween(LocalDate from, LocalDate to, Sort sort);

    List<Task> findByStatus(Status status, Sort sort);
    List<Task> findByStatusIn(List<Status> statuses, Sort sort);
    List<Task> findByStatusAndUser(Status status, User user, Sort sort);
    List<Task> findByStatusInAndUser(List<Status> statuses, User user,Sort sort);

    List<Task> findByPriority(Priority priority, Sort sort);
    List<Task> findByPriorityIn(List<Priority> priorities, Sort sort);
    List<Task> findByPriorityAndUser(Priority priority, User user, Sort sort);
    List<Task> findByPriorityInAndUser(List<Priority> priorities, User user, Sort sort);

    List<Task> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to, Sort sort);
    List<Task> findByCreatedAtAfter(LocalDateTime createdAt, Sort sort);
    List<Task> findByCreatedAtBefore(LocalDateTime createdAt, Sort sort);

    List<Task> findByUser (User user, Sort sort);


    boolean existsByTaskId(int taskId);
    boolean existsByTitle(String title);

}
