package org.taskflow;
import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import netscape.javascript.JSObject;
import org.json.JSONObject;
import org.taskflow.command.LoginCommand;
import org.taskflow.service.TokenService;
import picocli.CommandLine;

import java.util.Base64;
import java.util.Date;

public class AuthSession {




    public static int getUserIdFromToken() {
        if (TokenService.getToken() == null || TokenService.getToken().trim().isEmpty()) {
            System.out.println("Token is not set or is empty");
            login();
        }

        try {
            if (TokenService.getToken() == null || TokenService.getToken().trim().isEmpty()) {
                throw new IllegalArgumentException("Token is null or empty");
            }
            DecodedJWT decodedJWT = JWT.decode(TokenService.getToken());

            Date expirationDate = decodedJWT.getExpiresAt();
            if(expirationDate.before(new Date())) {
                login();
            }

            String subject = decodedJWT.getSubject();
            if (subject == null) {
                throw new IllegalArgumentException("Subject claim is missing in the token");
            }

            return Integer.parseInt(subject);
        } catch (JWTDecodeException e) {
            System.out.println("Invalid token format");
            e.printStackTrace();
            return -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }


    private static void login(){
        CommandLine loginCommand = new CommandLine(new LoginCommand());
        loginCommand.execute();
    }
}
