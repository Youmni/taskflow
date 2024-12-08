package org.taskflow.command;

import org.taskflow.AuthSession;
import org.taskflow.command.group.rootcommand.GroupCommand;
import org.taskflow.command.task.rootcommand.TaskCommand;
import org.taskflow.command.taskhistory.root.TaskHistoryCommand;
import picocli.CommandLine;

@CommandLine.Command(name = "taskflow", description = "Taskflow is a CLI task manager", mixinStandardHelpOptions = true, version = "Taskflow version 1.0", subcommands = {LoginCommand.class, RegistrationCommand .class, GroupCommand.class, TaskCommand.class, TaskHistoryCommand.class, Whoami.class})
public class TaskFlowManager implements Runnable {




    @Override
    public void run() {
            System.out.println("Welcome to TaskFlow Manager\n");
            System.out.println("Use -h or --help to display the help menu and available commands");
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new TaskFlowManager()).execute(args);
        System.exit(exitCode);
    }
}
