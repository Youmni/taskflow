package org.taskflow.command.taskhistory.root;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.taskflow.AuthSession;
import org.taskflow.Inputvalidator;
import org.taskflow.enums.Permission;
import org.taskflow.model.TaskHistory;
import org.taskflow.service.TokenService;
import picocli.CommandLine;

import java.io.Console;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@CommandLine.Command(name = "history", description = "As the creator of a task, you have the right to view all versions of the changes made to your task", mixinStandardHelpOptions = true)
public class TaskHistoryCommand implements Runnable {

    @CommandLine.Option(names = {"-i", "--taskID"}, description = "The ID of the task whose history you want to view")
    private String taskId;


    @Override
    public void run() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            taskId = getInputValidated(taskId, input -> input != null && !input.trim().isEmpty() && isValidInteger(input));

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/taskhistory/" + taskId+"/ID?userId="+ AuthSession.getUserIdFromToken()))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + TokenService.getToken())
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            handleResponse(response, mapper);


        } catch (Exception e) {
            System.out.println("Cannot get history from Task with ID: " + taskId);;
        }




    }
    private String getInputValidated(String value, Inputvalidator validator) {
        String input = value;
        while (input == null || !validator.isValid(input.trim())) {
            if (input != null) {
                System.out.println("Task ID is required");
            }
            input = getInput(null);
        }
        return input.trim();
    }
    private String getInput(String value){
        if(value == null){
            Console console = System.console();
            return console.readLine("Task ID: ");
        }else{
            return value;
        }
    }
    private boolean isValidInteger(String input){
        try{
            Integer.parseInt(input);
            return true;
        }catch (NumberFormatException e){
            return false;
        }
    }
    private void handleResponse(HttpResponse<String> response, ObjectMapper mapper) {
        try {
            if(response.statusCode() == 200 && response.body() != null && !response.body().isEmpty()) {

                List<TaskHistory> tasks = mapper.readValue(response.body(), new TypeReference<List<TaskHistory>>() {});

                if(tasks.isEmpty()){
                    System.out.println("The task could not be accessed. It may not exist, or you might not have the necessary permissions to view it.");
                }

                for (TaskHistory task : tasks) {
                    System.out.println("----------------------------------");
                    System.out.println("Task ID: " + task.getTaskId());
                    System.out.println("History ID: " + task.getHistoryId());
                    System.out.println("Title: " + task.getTitle());
                    System.out.println("Description: " + task.getDescription());
                    System.out.println("Status: " + task.getStatus());
                    System.out.println("Priority: " + task.getPriority());
                    System.out.println("Deadline: " + task.getDueDate());
                    System.out.println("Comment: " + task.getComment());
                    System.out.println("User ID: " + task.getUserId());
                    if (task.getGroups() != null && !task.getGroups().isEmpty()) {
                        System.out.println("Groups added: ");
                        for (Map.Entry<Integer, Permission> groupPermission : task.getGroups().entrySet()) {
                            System.out.println("--> " + groupPermission.getKey() + ": " + groupPermission.getValue());
                        }
                    }else {
                        System.out.println("--> No groups available.");
                    }
                    System.out.println("----------------------------------");
                }

            }else{
                System.out.println("Error: "+ response.statusCode());
            }
        } catch (JsonProcessingException e) {
            System.err.println("Failed to parse the response: " + e.getMessage());
        }
    }
}
