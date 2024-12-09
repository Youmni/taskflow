package org.taskflow.command.group.subcommand;

import org.taskflow.AuthSession;
import org.taskflow.Inputvalidator;
import org.taskflow.service.TokenService;
import picocli.CommandLine;

import java.io.Console;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

@CommandLine.Command(name = "delete", description = "Delete a group you made", mixinStandardHelpOptions = true)
public class DeleteGroupCommand implements Runnable {

    @CommandLine.Option(names = {"-g", "--group"}, description = "Group you want to delete")
    private String groupId;

    @Override
    public void run() {
        try {
            groupId = getInputValidated("Group ID: ", groupId, input -> input != null && !input.trim().isEmpty() && isValidInteger(input), "Group ID is required");

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/group/"+groupId+"/remove?ownerId="+AuthSession.getUserIdFromToken()))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + TokenService.getToken())
                    .DELETE()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            handleResponse(response);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
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
    private void handleResponse(HttpResponse<String> response) {
        System.out.println(response.body());
    }
}
