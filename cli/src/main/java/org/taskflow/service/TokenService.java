package org.taskflow.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class TokenService {

    private static final String TOKEN_FILE = "token.txt";

    public static String getToken() {
        String token = null;

        try (BufferedReader reader = new BufferedReader(new FileReader(TOKEN_FILE))) {
            token = reader.readLine();  // Leest de eerste regel van het bestand (de token)
        } catch (IOException e) {
            System.out.println("Fout bij het lezen van het token uit het bestand.");
            e.printStackTrace();
        }

        return token;
    }

    public static String readTokenFromFile(){
        String token = null;
        try (BufferedReader reader = new BufferedReader(new FileReader(TOKEN_FILE))) {
            token = reader.readLine();
        } catch (IOException e) {
            System.out.println("Problem reading token file");
            e.printStackTrace();
        }
        return token;
    }

    public static void saveTokenToFile(String token){
        try(FileWriter writer = new FileWriter(TOKEN_FILE, false)){
            writer.write(token);
        }catch(IOException e){
            System.out.println("Problem writing token file");
        }
    }
}
