package org.taskflow.command;

import org.taskflow.AuthSession;
import org.taskflow.service.TokenService;
import picocli.CommandLine;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@CommandLine.Command(name = "whoami", description = "Returns the username of the user that is logged in")
public class Whoami implements Runnable {


    @Override
    public void run() {

        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/user/"+ AuthSession.getUserIdFromToken()))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + TokenService.getToken())
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("You are logged in as: " + response.body());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


    }
}
