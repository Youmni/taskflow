package org.taskflow.command.task.rootcommand;

import org.taskflow.command.task.subcommand.CreateTask;
import org.taskflow.command.task.subcommand.DeleteTask;
import org.taskflow.command.task.subcommand.ListTask;
import org.taskflow.command.task.subcommand.UpdateTask;
import picocli.CommandLine;

@CommandLine.Command(name = "task", description = "Manage and configure tasks in the system. Use subcommands to create tasks, assign users to tasks, and list all tasks", subcommands = {CreateTask.class, ListTask.class, UpdateTask.class, DeleteTask.class}, mixinStandardHelpOptions = true)
public class TaskCommand implements Runnable {


    @Override
    public void run() {
        System.out.println("Use subcommands like 'create', 'update', or 'list' for task management");
    }
}
