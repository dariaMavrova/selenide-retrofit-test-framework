package org.example.api.ae.model;

/**
 * AutomationExercise answers every request with HTTP 200; the real status is {@code responseCode}.
 */
public record MessageResponse(int responseCode, String message) {
}
