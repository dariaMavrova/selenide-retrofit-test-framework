package org.example.config;

/**
 * Central test configuration. Every value can be overridden with -D system properties,
 * e.g. ./gradlew apiTest -Dapi.booker.url=https://staging.example.com/
 */
public final class Config {

    private Config() {
    }

    public static String bookerUrl() {
        return get("api.booker.url", "https://restful-booker.herokuapp.com/");
    }

    public static String bookerUsername() {
        return get("api.booker.username", "admin");
    }

    public static String bookerPassword() {
        return get("api.booker.password", "password123");
    }

    public static String aeUrl() {
        return get("ui.ae.url", "https://automationexercise.com");
    }

    public static String aeApiUrl() {
        return get("api.ae.url", aeUrl() + "/api/");
    }

    public static String herokuappUrl() {
        return get("ui.herokuapp.url", "https://the-internet.herokuapp.com");
    }

    private static String get(String key, String defaultValue) {
        return System.getProperty(key, defaultValue);
    }
}
