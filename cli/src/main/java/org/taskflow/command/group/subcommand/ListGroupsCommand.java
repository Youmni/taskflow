package org.taskflow.command.group.subcommand;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.taskflow.AuthSession;
import org.taskflow.dtos.GroupRequestDTO;
import org.taskflow.service.TokenService;
import picocli.CommandLine;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@CommandLine.Command(name = "list", description = "Description of all your groups", mixinStandardHelpOptions = true)
public class ListGroupsCommand implements Runnable {


    @Override
    public void run() {

        try {

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/group/"+ AuthSession.getUserIdFromToken()))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + TokenService.getToken())
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            handleResponse(response);

        } catch (Exception e) {
            System.err.println("An error occurred while trying to log in: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private void handleResponse(HttpResponse<String> response) {
        try {
            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();

                List<GroupRequestDTO> groups = mapper.readValue(response.body(), new TypeReference<List<GroupRequestDTO>>() {});

                for (GroupRequestDTO group : groups) {
                    System.out.println("----------------------------------");
                    System.out.println("Group ID: " + group.getGroupId());
                    System.out.println("Group Name: " + group.getGroupName());
                    System.out.println("Description: " + group.getDescription());
                    System.out.println("Emails: ");

                    for(String email: group.getEmails()){
                        System.out.println("--> " + email);
                    }
                    System.out.println("----------------------------------");
                }
            } else {
                System.out.println("Error: " + response.body());
            }
        } catch (JsonProcessingException e) {
            System.err.println("Failed to parse the response: " + e.getMessage());
        }
    }

}
