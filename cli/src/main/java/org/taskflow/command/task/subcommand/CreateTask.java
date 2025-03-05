package org.taskflow.command.task.subcommand;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.taskflow.AuthSession;
import org.taskflow.dtos.TaskCreationDTO;
import org.taskflow.dtos.TaskDTO;
import org.taskflow.Inputvalidator;
import org.taskflow.enums.Permission;
import org.taskflow.enums.Priority;
import org.taskflow.enums.Status;
import org.taskflow.service.TokenService;
import picocli.CommandLine;

import java.io.Console;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;

@CommandLine.Command(name = "create", description = "Create a new task", mixinStandardHelpOptions = true)
public class CreateTask implements Runnable {

    @CommandLine.Option(names = {"-t", "--title"}, description = "Add title for the task")
    private String title;

    @CommandLine.Option(names = {"-d", "--description"}, description = "Add a description for the task")
    private String description;

    @CommandLine.Option(names = {"-s", "--status"}, description = "Add a status for the task. Accepted values are: IN_PROGRESS, COMPLETED, CANCELLED, FAILED, VERIFIED, OVERDUE")
    private Status status;

    @CommandLine.Option(names = {"-p", "--priority"}, description = "Add a priority for the task. Accepted values are: LOW, NORMAL, HIGH")
    private Priority priority;

    @CommandLine.Option(names = {"--dueDate"}, description = "Add a deadline for the task. Formatted: yyyy-mm-dd")
    private LocalDate dueDate;

    @CommandLine.Option(names = {"-c", "--comment"}, description = "Add a comment for the task. This could give more details to the user")
    private String comment;

    private int userId;

    @CommandLine.Option(names = {"--permissions"}, description = "Add the permissions for groups in the task (format: groupID:permission). Multiple entries can be provided")
    private HashMap<Integer, Permission> group;


    @Override
    public void run() {

        title = getInputValidated("Title: ", title, input -> input != null && input.trim().length() >=5 && input.trim().length()<=40, "Title must be between 5 and 40 characters");
        description = getInputValidated("Description: ", description, input -> input != null && input.trim().length() >= 25 && input.trim().length()<=512, "Description must be between 25 and 512 characters");

        String statusInput = getInputValidated("Status (IN_PROGRESS, COMPLETED, CANCELLED, FAILED, VERIFIED, OVERDUE): ", null, input -> isValidEnum(Status.class, input), "Status is required. Accepted values are: IN_PROGRESS, COMPLETED, CANCELLED, FAILED, VERIFIED, OVERDUE");
        status = Status.valueOf(statusInput.trim().toUpperCase());

        String priorityInput = getInputValidated("Priority (LOW, NORMAL, HIGH): ", null, input -> isValidEnum(Priority.class, input), "Priority is required. Accepted values are: LOW, NORMAL, HIGH");
        priority = Priority.valueOf(priorityInput.trim().toUpperCase());

        String dueDateInput = getInputValidated("Deadline (yyyy-mm-dd) (today -> 2100-12-31): ", null, input -> input == null || input.isEmpty() || (isValidDateFormat(input) && isDateInRange(input)), "Invalid date format. Please enter in yyyy-mm-dd format.");
        if (dueDateInput != null && !dueDateInput.isEmpty()) dueDate = LocalDate.parse(dueDateInput);


        comment = getInputValidated("Comment: ", null, input -> input.length()<=512, "Comment must be between 0 and 512 characters");

        group = new HashMap<>();
        String groupPermissionsInput = getInput("""
                --> Permission is required. Accepted values are: READ, WRITE, DELETE.
                Where:
                READ allows read access.
                WRITE includes read access and allows write access.
                DELETE includes read and write access, and allows delete access.
                
                Group permissions (groupID:permission), or 'q' to finish:\s""", null);
        while (groupPermissionsInput != null && !groupPermissionsInput.equalsIgnoreCase("q")) {
            String[] parts = groupPermissionsInput.split(":");
            if (parts.length == 2) {
                try {
                    Integer groupId = Integer.parseInt(parts[0]);
                    Permission permission = Permission.valueOf(parts[1].toUpperCase());
                    group.put(groupId, permission);
                } catch (IllegalArgumentException e) {
                    System.out.println("Invalid input for group permissions. Please enter in format 'groupID:permission' (e.g., 1:READ).");
                }
            } else {
                System.out.println("Invalid format for group permissions. Please use 'groupID:permission'.");
            }
            groupPermissionsInput = getInput("Add another group permission or 'q' to finish: ", null);
        }

        handleResponse(createTask());

    }
    private String getInputValidated(String prompt, String value, Inputvalidator validator, String errorMSG) {
        String input = value;
        while (input == null || !validator.isValid(input.trim())) {
            if (input != null) {
                System.out.println(errorMSG);
            }
            input = getInput(prompt, null);
        }
        return input.trim();
    }
    private String getInput(String prompt, String value){
        if(value == null){
            Console console = System.console();
            return console.readLine(prompt);
        }else{
            return value;
        }
    }
    private boolean isValidInteger(String input, int min, int max){
        try{
            int value = Integer.parseInt(input);
            return value >= min && value <= max;
        }catch (NumberFormatException e){
            return false;
        }
    }
    private boolean isValidDateFormat(String input){
        try{
            LocalDate.parse(input);
            return true;
        }catch (Exception e){
            return false;
        }
    }
    private boolean isDateInRange(String input){
        try{
            LocalDate date = LocalDate.parse(input);
            LocalDate now = LocalDate.now();
            LocalDate upper = LocalDate.of(2100,12,31);

            return !date.isBefore(now) && !date.isAfter(upper);
        }catch (DateTimeParseException e){
            return false;
        }
    }
    private <T extends Enum<T>> boolean isValidEnum(Class<T> enumClass, String input){
        try{
            Enum.valueOf(enumClass, input.toUpperCase());
            return true;
        }catch (IllegalArgumentException e){
            return false;
        }
    }

    private HttpResponse<String> createTask() {
        try {
            TaskDTO taskRequest = new TaskDTO();
            taskRequest.setTitle(title);
            taskRequest.setDescription(description);
            taskRequest.setDueDate(dueDate);
            taskRequest.setStatus(status);
            taskRequest.setPriority(priority);
            taskRequest.setComment(comment);
            taskRequest.setUserId(AuthSession.getUserIdFromToken());

            TaskCreationDTO taskCreationDTO = new TaskCreationDTO();
            taskCreationDTO.setTaskDTO(taskRequest);
            taskCreationDTO.setGroup(group);

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            String json = objectMapper.writeValueAsString(taskCreationDTO);

            HttpClient httpClient = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/task/create"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + TokenService.getToken())
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            return response;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private void handleResponse(HttpResponse<String> response){
        if(response.statusCode() == 200){
            System.out.println("Created task successfully!");
        }else{
            System.out.println(response.body());
        }
    }
}
