package org.taskflow.command.group.rootcommand;

import org.taskflow.command.group.subcommand.*;
import picocli.CommandLine;

@CommandLine.Command(name = "group", description = "Manage and configure groups in the system. Use subcommands to create groups, add users to groups, and list all groups", mixinStandardHelpOptions = true, subcommands = {CreateGroupCommand.class, ListGroupsCommand.class, AddToGroupCommand.class, RemoveFromGroupCommand.class, DeleteGroupCommand.class})
public class GroupCommand implements Runnable {

    @Override
    public void run() {
        System.out.println("Use subcommands like 'create', 'add', or 'list' for group management.");
    }
}
