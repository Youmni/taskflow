package org.taskflow.command.group.subcommand;

import org.taskflow.AuthSession;
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
    private int groupId;

    @Override
    public void run() {
        try {
            groupId =  groupId > 0 ? groupId : Integer.parseInt(getInput("Group ID: ", null));

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
    private String getInput(String prompt, String value){
        if(value == null){
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
    private void handleResponse(HttpResponse<String> response) {
        System.out.println(response.body());
    }
}
