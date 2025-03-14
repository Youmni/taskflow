package org.taskflow.command.group.subcommand;

import org.taskflow.AuthSession;
import org.taskflow.Inputvalidator;
import org.taskflow.service.TokenService;
import picocli.CommandLine;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Console;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@CommandLine.Command(name = "create", description = "Create a group for task management. When creating a task, you can assign a group to facilitate sharing and collaboration", mixinStandardHelpOptions = true)
public class CreateGroupCommand implements Runnable {

    @CommandLine.Option(names = {"-n", "--name"}, description = "Name for the new group")
    private String groupName;

    @CommandLine.Option(names = {"-d", "--description"}, description = "Description for the new group")
    private String description;

    private List<String> emails = new ArrayList<>();
    private final int userId = AuthSession.getUserIdFromToken();


    @Override
    public void run() {

        Console console = System.console();
        groupName = getInputValidated("Enter the name of the group:", groupName, input -> input.trim().length() >=5 && input.length()<=20, "Name of group must be between 5 and 20 characters");
        description = getInputValidated("Enter the description of the group:", description, input -> input.trim().length()<=80, "Description must be between 0 and 80 characters");

        String answer = console.readLine("Would you like to add users to this group now? (Y/n)");
        if (answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes")) {
            boolean isAddingUsers = true;

            System.out.println("Enter 'q' or 'exit' to stop");
            while (isAddingUsers) {
                String output = console.readLine("Enter the email address of the user:");
                if(output == null || output.isEmpty() || output.trim().equalsIgnoreCase("q") || output.trim().equalsIgnoreCase("exit")) {
                    isAddingUsers = false;
                }else{
                    emails.add(output);
                }
            }
        }

        if(emails.isEmpty()) {
            createGroupWithoutUsers();
        }else {
            createGroupWithUsers();
        }
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

    private void createGroupWithUsers() {
        try {
            String jsonPayload = String.format("{\"groupName\": \"%s\", \"description\": \"%s\", \"emails\": %s}",
                    groupName, description, new ObjectMapper().writeValueAsString(emails));
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/group/createWithUsers/"+userId))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + TokenService.getToken())
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            handleResponse(response);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void createGroupWithoutUsers() {
        try {
            String jsonPayload = String.format("{\"groupName\": \"%s\", \"description\": \"%s\"}", groupName, description);

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/group/createWithoutUsers/"+userId))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + TokenService.getToken())
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            handleResponse(response);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleResponse(HttpResponse<String> response){
        if(response.statusCode() == 201){
            System.out.println("Group successfully created!");
        }else{
            System.out.println(response.body());
        }
    }
}
