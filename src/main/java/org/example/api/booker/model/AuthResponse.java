package org.example.api.booker.model;

/** On bad credentials the API still answers 200, with {@code reason} set instead of {@code token}. */
public record AuthResponse(String token, String reason) {
}
