package org.taskflow.command;

import org.taskflow.AuthSession;
import org.taskflow.Inputvalidator;
import picocli.CommandLine;

import java.io.Console;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;
import java.util.regex.Pattern;

@CommandLine.Command(name = "registration", description = "Register a new user for the system", mixinStandardHelpOptions = true)
public class RegistrationCommand implements Runnable {

    @CommandLine.Option(names = {"-u", "--username"}, description = "Provide a username")
    private String username;

    @CommandLine.Option(names = {"-e", "--email"}, description = "Provide an email address")
    private String email;

    @CommandLine.Option(names = {"-p", "--password"}, description = "Provide a password")
    private String password;


    @Override
    public void run() {
        try{

            username = getInputValidated("Username: ", username, input -> input.length() >= 5 && input.length() <=15, "Username must be between 5 and 15 characters");
            email = getInputValidated("Email: ", email, input -> isValidEmail(email), "Email must be a valid email address");
            password = getInputValidated("Password: ", password, input ->input.length()>=8 && input.length()<=50,"Password must be between 8 and 50 characters");

            String jsonPayload = String.format("{\"username\": \"%s\", \"email\": \"%s\", \"password\": \"%s\"}", username, email, password);

            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/user/create"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            handleResponse(response);

        }catch(Exception e){
            e.printStackTrace();
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

    private void handleResponse(HttpResponse<String> response){
        if(response.statusCode() == 201){
            System.out.println("User registered successfully");
        }else{
            System.out.println(response.body());
        }
    }
    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return Pattern.compile(emailRegex).matcher(email).matches();
    }
}
