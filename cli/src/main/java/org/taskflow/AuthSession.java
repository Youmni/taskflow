package org.taskflow;

public class AuthSession {

    private static String token;

    public static void setToken(String token) {
        AuthSession.token = token;
    }

    public static String getToken() {
        return token;
    }

    public static boolean hasToken() {
        return token != null;
    }

    public static void clearToken() {
        token = null;
    }
}
