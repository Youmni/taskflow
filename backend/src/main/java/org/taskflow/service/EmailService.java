package org.taskflow.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.taskflow.enums.Permission;
import org.taskflow.models.Task;
import org.thymeleaf.spring6.SpringTemplateEngine;

import org.thymeleaf.context.Context;
import java.util.HashMap;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    public EmailService(JavaMailSender mailSender, SpringTemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public void sendTaskCreatedEmail(Task task, String toEmail, Permission permission) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(message, true);

        HashMap<String, Object> taskAttributes = new HashMap<>();
        taskAttributes.put("title", task.getTitle());
        taskAttributes.put("description", task.getDescription());
        taskAttributes.put("dueDate", task.getDueDate());
        taskAttributes.put("creator", task.getUser().getEmail());
        taskAttributes.put("permission", permission);


        Context context = new Context();
        context.setVariables(taskAttributes);

        String htmlContent = templateEngine.process("task-created-email", context);

        mimeMessageHelper.setTo(toEmail);
        mimeMessageHelper.setSubject("New task created by "+task.getUser().getUsername());
        mimeMessageHelper.setText(htmlContent, true);

        mailSender.send(message);
    }

    public void sendTaskReminderEmail(Task task, String toEmail, Permission permission) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(message, true);

        HashMap<String, Object> taskAttributes = new HashMap<>();
        taskAttributes.put("taskId", task.getTaskId());
        taskAttributes.put("title", task.getTitle());
        taskAttributes.put("description", task.getDescription());
        taskAttributes.put("dueDate", task.getDueDate());
        taskAttributes.put("creator", task.getUser().getEmail());
        taskAttributes.put("permission", permission);


        Context context = new Context();
        context.setVariables(taskAttributes);

        String htmlContent = templateEngine.process("task-reminder-email", context);

        mimeMessageHelper.setTo(toEmail);
        mimeMessageHelper.setSubject("New task created by "+task.getUser().getUsername());
        mimeMessageHelper.setText(htmlContent, true);

        mailSender.send(message);
    }
}
