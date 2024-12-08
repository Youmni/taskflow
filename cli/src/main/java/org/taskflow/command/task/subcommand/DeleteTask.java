package org.taskflow.command.task.subcommand;

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

@CommandLine.Command(name = "delete", description = "Delete a task")
public class DeleteTask implements Runnable {

    @CommandLine.Option(names = {"-i", "--taskID"}, description = "Give the task ID")
    private String taskId;

    private boolean isSure;

    @Override
    public void run() {
        try{
            taskId = getInputValidated("Task ID: ", taskId, input -> input != null && !input.trim().isEmpty() && isValidInteger(input), "Task ID is required");

            Console console = System.console();

            String confirmation = "";
            while((!confirmation.equalsIgnoreCase("y") && !confirmation.equalsIgnoreCase("n"))) {
                confirmation = console.readLine("Are you sure you want to delete this task? (y/N): ");

                if(!confirmation.equalsIgnoreCase("y") && !confirmation.equalsIgnoreCase("n")) {
                    System.out.println("Invalid input\n");
                }
            }
            isSure = confirmation.equalsIgnoreCase("y");

            if(isSure) {

                HttpClient client = HttpClient.newHttpClient();

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:8080/task/"+taskId+"/delete?userId="+ AuthSession.getUserIdFromToken()))
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + TokenService.getToken())
                        .DELETE()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                handleResponse(response);
            }else{
                System.out.println("Task with ID: "+taskId+" not deleted");
            }
        }catch (IOException | InterruptedException e) {
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