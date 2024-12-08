package org.taskflow.command.task.subcommand;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.taskflow.AuthSession;
import org.taskflow.DTO.TaskCreationRequest;
import org.taskflow.DTO.TaskRequest;
import org.taskflow.Inputvalidator;
import org.taskflow.enums.Permission;
import org.taskflow.enums.Priority;
import org.taskflow.enums.Status;
import org.taskflow.service.TokenService;
import picocli.CommandLine;

import java.io.Console;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@CommandLine.Command(name = "update", description = "Update a task", mixinStandardHelpOptions = true)
public class UpdateTask implements Runnable {

    @CommandLine.Option(names = {"-i", "--taskID"}, description = "Give the task ID")
    private String taskId;

    @CommandLine.Option(names = {"-t", "--title"}, description = "Update title for the task")
    private String title;

    @CommandLine.Option(names = {"-d", "--description"}, description = "Update a description for the task")
    private String description;

    @CommandLine.Option(names = {"-s", "--status"}, description = "Update a status for the task. Accepted values are: IN_PROGRESS, COMPLETED, CANCELLED, FAILED, VERIFIED, OVERDUE")
    private Status status;

    @CommandLine.Option(names = {"-p", "--priority"}, description = "Update a priority for the task. Accepted values are: LOW, NORMAL, HIGH")
    private Priority priority;

    @CommandLine.Option(names = {"--dueDate"}, description = "Update a deadline for the task. Formatted: yyyy-mm-dd")
    private LocalDate dueDate;

    @CommandLine.Option(names = {"-c", "--comment"}, description = "Update a comment for the task. This could give more details to the user")
    private String comment;

    private int userIdMakingChanges;

    @Override
    public void run() {
        taskId = getInputValidated("Task ID: ", taskId, input -> input != null && !input.trim().isEmpty() && isValidInteger(input), "Task ID is required");
        title = getInputValidated("Title: ", title, input -> input == null || input.trim().isEmpty() || ( input.trim().length()  >= 5 && input.trim().length() <= 40), "Title must be between 5 and 40 characters");
        description = getInputValidated("Description: ", description, input -> input == null || input.trim().isEmpty() || (input.trim().length() >= 25 && input.trim().length() <= 512), "Description must be between 25 and 512 characters");

        String statusInput = getInputValidated("Status (IN_PROGRESS, COMPLETED, CANCELLED, FAILED, VERIFIED, OVERDUE): ", null, input -> input == null || input.trim().isEmpty() || isValidEnum(Status.class, input), "Status is required. Accepted values are: IN_PROGRESS, COMPLETED, CANCELLED, FAILED, VERIFIED, OVERDUE");
        if (statusInput != null && !statusInput.trim().isEmpty()) {
            status = Status.valueOf(statusInput.trim().toUpperCase());
        } else {
            status = null;
        }

        String priorityInput = getInputValidated("Priority (LOW, NORMAL, HIGH): ", null, input -> input == null || input.trim().isEmpty() || isValidEnum(Priority.class, input), "Priority is required. Accepted values are: LOW, NORMAL, HIGH");
        if (priorityInput != null && !priorityInput.trim().isEmpty()) {
            priority = Priority.valueOf(priorityInput.trim().toUpperCase());
        } else {
            priority = null;
        }

        String dueDateInput = getInputValidated("Deadline (yyyy-mm-dd) (today -> 2100-12-31): ", null, input -> input == null || input.isEmpty() || (isValidDateFormat(input) && isDateInRange(input)), "Invalid date format. Please enter in yyyy-mm-dd format.");
        if (dueDateInput != null && !dueDateInput.isEmpty()) dueDate = LocalDate.parse(dueDateInput);

        comment = getInputValidated("Comment: ", null, input -> input == null || input.trim().isEmpty() || input.length()<=512, "Comment must be between 0 and 512 characters");

        handleResponse(updateTask());

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
            if (input == null || input.trim().isEmpty()) {
                return false;
            }
            Enum.valueOf(enumClass, input.toUpperCase());
            return true;
        }catch (IllegalArgumentException e){
            return false;
        }
    }
    private boolean isValidInteger(String input){
        try{
            int value = Integer.parseInt(input);
            return true;
        }catch (NumberFormatException e){
            return false;
        }
    }
    private HttpResponse<String> updateTask() {
        try {

            boolean isChanged = false;
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            HttpClient client = HttpClient.newHttpClient();
            StringBuilder urlBuilder = new StringBuilder("http://localhost:8080/task/" + Integer.valueOf(taskId)+ "/update?");

            if (title != null && !title.isEmpty()) {
                urlBuilder.append("title=").append(URLEncoder.encode(title, StandardCharsets.UTF_8)).append("&");
                isChanged = true;
            }
            if (description != null && !description.isEmpty()) {
                urlBuilder.append("description=").append(URLEncoder.encode(description, StandardCharsets.UTF_8)).append("&");
                isChanged = true;
            }
            if (status != null) {
                urlBuilder.append("status=").append(URLEncoder.encode(status.name(), StandardCharsets.UTF_8)).append("&");
                isChanged = true;
            }
            if (priority != null) {
                urlBuilder.append("priority=").append(URLEncoder.encode(priority.name(), StandardCharsets.UTF_8)).append("&");
                isChanged = true;
            }
            if (dueDate != null) {
                urlBuilder.append("dueDate=").append(URLEncoder.encode(dueDate.toString(), StandardCharsets.UTF_8)).append("&");
                isChanged = true;
            }
            if (comment != null && !comment.isEmpty()) {
                urlBuilder.append("comment=").append(URLEncoder.encode(comment, StandardCharsets.UTF_8)).append("&");
                isChanged = true;
            }


            if (isChanged) {
                userIdMakingChanges = AuthSession.getUserIdFromToken();
                urlBuilder.append("userIdMakingChanges=").append(URLEncoder.encode(String.valueOf(userIdMakingChanges), StandardCharsets.UTF_8)).append("&");
            }

            String url = urlBuilder.toString();
            if (url.endsWith("&")) {
                url = url.substring(0, url.length() - 1);
            }
            if (isChanged) {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + TokenService.getToken())
                        .PUT(HttpRequest.BodyPublishers.noBody())
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                return response;
            } else {
                return null;
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private void handleResponse(HttpResponse<String> response){
        if(response != null && response.statusCode() == 200){
            System.out.println("Created task successfully!");
        }else if( response == null){
            System.out.println("No changes made");
        }else{
            System.out.println(response.body());
        }
    }
}
