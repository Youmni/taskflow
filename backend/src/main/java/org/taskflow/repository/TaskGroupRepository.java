package org.taskflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.taskflow.models.Group;
import org.taskflow.enums.Permission;
import org.taskflow.models.Task;
import org.taskflow.models.TaskGroup;

import java.util.List;

public interface TaskGroupRepository extends JpaRepository<TaskGroup, Integer> {

    List<TaskGroup> findByTask(Task task);
    List<TaskGroup> findByGroup(Group group);
    List<TaskGroup> findByPermission( Permission permission);
    List<TaskGroup> findByTaskAndGroup(Task task, Group group);
    List<TaskGroup> findByPermissionAndTask (Permission permission, Task task);
    List<TaskGroup> findByPermissionAndGroup(Permission permission, Group group);
    List<TaskGroup> findByPermissionAndGroupAndTask(Permission permission, Group group, Task task);

}
