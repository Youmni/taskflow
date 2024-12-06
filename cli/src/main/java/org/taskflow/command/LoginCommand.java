package org.taskflow.command;

import org.taskflow.AuthSession;
import picocli.CommandLine;

import java.io.Console;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

@CommandLine.Command(name = "login", description = "Login to use the taskflow service!")
public class LoginCommand implements Runnable {

    @CommandLine.Option(names = {"-u", "--username"}, description = "Provide a username")
    private String username;

    @CommandLine.Option(names = {"-p", "--password"}, description = "Provide a password")
    private String password;


    @Override
    public void run() {

        try {

            username = getInput("Username: ", username);
            password = getInput("Password: ", password);

            String jsonPayload = String.format("{\"username\": \"%s\", \"password\": \"%s\"}", username, password);

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/user/authenticate"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            handleResponse(response);

        } catch (Exception e) {
            System.err.println("An error occurred while trying to log in: " + e.getMessage());
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

    private void handleResponse(HttpResponse<String> response){
        if(response.statusCode() == 200){
            System.out.println("Login successful!");
        }else{
            System.out.println(response.body());
        }
    }
}
