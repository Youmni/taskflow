package org.taskflow.command.task.subcommand;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.taskflow.AuthSession;
import org.taskflow.DTO.GroupRequestDTO;
import org.taskflow.DTO.TaskDTO;
import org.taskflow.enums.Permission;
import org.taskflow.enums.Priority;
import org.taskflow.enums.Status;
import org.taskflow.service.TokenService;
import picocli.CommandLine;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@CommandLine.Command(name = "list", description = "Overview of all your tasks", mixinStandardHelpOptions = true)
public class ListTask implements Runnable {

    @CommandLine.Option(names = {"-d", "--deadline"}, description = "Deadline of the task")
    private LocalDate dueDate;

    @CommandLine.Option(names = {"-dA", "--deadlineAfter"}, description = "Specify the date to filter tasks with a deadline after the given date")
    private LocalDate dueDateAfter;

    @CommandLine.Option(names = {"-dB", "--deadlineBefore"}, description = "Specify the date to filter tasks with a deadline before the given date")
    private LocalDate dueDateBefore;

    @CommandLine.Option(names = {"-p", "--priority"}, description = "Specify the priority of the task")
    private Priority priority;

    @CommandLine.Option(names = {"-s", "--status"}, description = "Specify the status of the task")
    private Status status;

    @CommandLine.Option(names = {"--shared"}, description = "All tasks, including those shared with you")
    private boolean isShared;

    @Override
    public void run() {
        try {

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            HttpClient client = HttpClient.newHttpClient();

            StringBuilder urlBuilder;

            if(isShared) {
                urlBuilder= new StringBuilder("http://localhost:8080/task/shared/" + AuthSession.getUserIdFromToken() + "/tasks?");
            }else{
                urlBuilder = new StringBuilder("http://localhost:8080/task/" + AuthSession.getUserIdFromToken() + "/tasks?");
            }


            if (dueDate != null) {
                urlBuilder.append("dueDate=").append(dueDate).append("&");
            }
            if (dueDateAfter != null) {
                urlBuilder.append("dueDateAfter=").append(dueDateAfter).append("&");
            }
            if (dueDateBefore != null) {
                urlBuilder.append("dueDateBefore=").append(dueDateBefore).append("&");
            }
            if (priority != null) {
                urlBuilder.append("priority=").append(priority).append("&");
            }
            if (status != null) {
                urlBuilder.append("status=").append(status).append("&");
            }

            String url = urlBuilder.toString();
            if (url.endsWith("&")) {
                url = url.substring(0, url.length() - 1);
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + TokenService.getToken())
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            handleResponse(response, mapper);

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleResponse(HttpResponse<String> response, ObjectMapper mapper) {
        try {
            if(response.statusCode() == 200) {

                List<TaskDTO> tasks = mapper.readValue(response.body(), new TypeReference<List<TaskDTO>>() {});

                for (TaskDTO task : tasks) {
                    System.out.println("----------------------------------");
                    System.out.println("Task ID: " + task.getTaskId());
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
                System.out.println(response.body());
            }



        } catch (JsonProcessingException e) {
            System.err.println("Failed to parse the response: " + e.getMessage());
        }
    }
}