package org.taskflow.command.group.subcommand;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.taskflow.AuthSession;
import org.taskflow.service.TokenService;
import picocli.CommandLine;

import java.io.Console;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

@CommandLine.Command(name = "add", description = "Add new members to a group")
public class AddToGroupCommand implements Runnable {

    @CommandLine.Option(names = {"-g", "--group"}, description = "Give the ID of the group you want to add")
    private int groupId;
    @CommandLine.Option(names = {"-e", "--email"}, description = "Add emails to a group")
    private List<String> emails = new ArrayList<>();


    @Override
    public void run() {
        try {
            groupId =  groupId > 0 ? groupId : Integer.parseInt(getInput("Group ID: ", null));
            emails = getEmails(emails);

            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(emails);
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/group/"+groupId+"/add-users?ownerId="+ AuthSession.getUserIdFromToken()))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + TokenService.getToken())
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            handleResponse(response);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private String getInput(String prompt, String value){
        if(value == null || value.isEmpty()){
            Console console = System.console();
            if(console != null){
                return console.readLine(prompt);
            }else{
                Scanner scanner = new Scanner(System.in);
                return scanner.nextLine();
            }
        }
        return value;
    }

    private List<String> getEmails(List<String> emails){
        Console console = System.console();

        boolean isAddingUsers = true;
        while (isAddingUsers) {
            String output = console.readLine("Enter the email address of the user:");
            if(output == null || output.isEmpty() || output.trim().equalsIgnoreCase("q") || output.trim().equalsIgnoreCase("exit")) {
                isAddingUsers = false;
            }else{
                emails.add(output);
            }
        }
        return emails;
    };

    private void handleResponse(HttpResponse<String> response){

        try {
            ObjectMapper objectMapper = new ObjectMapper();

            Map<String, List<String>> result = objectMapper.readValue(response.body(), new TypeReference<Map<String, List<String>>>() {});

            System.out.println("Successes: ");
            List<String> succesEmail = result.get("Successes");
            if(succesEmail != null && !succesEmail.isEmpty()){
                for(String email : succesEmail){
                    System.out.println("--> " + email);
                }
            }

            System.out.println("Failures: ");
            List<String> failEmails = result.get("Failures");
            if(failEmails != null && !failEmails.isEmpty()){
                for(String email : failEmails){
                    System.out.println("--> " + email);
                }
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
