package org.example.api.reqres.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record UserListResponse(
        int page,
        @JsonProperty("per_page") int perPage,
        int total,
        @JsonProperty("total_pages") int totalPages,
        List<User> data) {
}
