# Taskflow
## Overview
Taskflow is a CLI tool for creating and managing tasks. When a user creates a task and assigns it to a group, they can define the actions that group can perform on that task. Group members are notified whenever they are added to a task. Permissions can also be set for each group to control whether they can edit or delete the task. Additionally, every change made to a task is recorded in the task history.
## Functions

### Task
- **Create a task:** A user can create a task.
- **View tasks:** A user can get an overview of all their tasks.
- **View shared tasks:** A user can get an overview of all the tasks shared with them.
- **Edit or delete tasks:** A user (or a member from a group they added, if granted the right permissions) can edit or delete a task.

### Group
- **Create a group:** A user can create a group.
- **Manage group members:** A user can add and remove people from the group.
- **Delete a group:** A user can delete a group.
- **View groups:** A user can get an overview of all their groups.

### History
- **View task history:** A user can view the history of their tasks, showing all changes made.

### User
- **View login status:** A user can see as who they are logged in.
- **Login:** A user can log in.
- **Register:** A user can register a new account.

## Installation
### Requirments 
- **JDK 21:** Ensure you have JDK 21 installed to run the project.
- **MySQL:** MySQL is required for database management. Make sure you have it installed and configured.
### Steps
**clone the project from github**
### Backend
1. Navigate to the `/backend` directory.
2. Load Maven.
3. Open the project structure and select the correct SDK (JDK version 21).
4. Go to `backend/src/main/resources` and create an `application.properties` file. Add your database connection details, email service information, and JWT secret in the file.
5. Once Maven is loaded and the `application.properties` file is configured, you can start the `BackendServer`.

### CLI
1. Navigate to the `/cli` directory.
2. Open the project structure from the menu.
3. Go to **Artifacts**.
4. Add a new artifact: Select **JAR** from "Modules and Dependencies".
5. After the artifact is created, go to the **Build** menu.
6. Select **Build Artifacts** to build the JAR file.
7. Set the full path of the generated JAR file as a system variable and name it `CLI_PATH` (or any name you prefer).
8. Open PowerShell and run the following command:
   ```powershell
   java -jar $env:CLI_PATH
9. Use the `-h` flag in case you need help.
10. You're all set and ready to go!

### Technologies
1. **Spring Boot**: Used for building the backend of the application.
2. **MySQL**: Used for managing the database.
3. **Picocli**: Used for creating the Command-Line Interface (CLI).

### Author
- [@Youmni Malha](https://github.com/Youmni)

