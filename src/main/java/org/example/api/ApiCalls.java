package org.example.api;

import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Executes Retrofit calls synchronously so tests don't deal with checked IOException.
 */
public final class ApiCalls {

    private ApiCalls() {
    }

    public static <T> Response<T> execute(Call<T> call) {
        try {
            return call.execute();
        } catch (IOException e) {
            throw new UncheckedIOException("Request failed: " + call.request(), e);
        }
    }

    /** Executes the call, asserts the expected status code and returns the body. */
    public static <T> T executeAndExpect(Call<T> call, int expectedStatus) {
        Response<T> response = execute(call);
        if (response.code() != expectedStatus) {
            throw new AssertionError("Expected HTTP " + expectedStatus + " but got " + response.code()
                    + " for " + call.request() + "\nBody: " + errorBody(response));
        }
        return response.body();
    }

    public static String errorBody(Response<?> response) {
        try (var body = response.errorBody()) {
            return body == null ? "" : body.string();
        } catch (IOException e) {
            return "<unreadable: " + e.getMessage() + ">";
        }
    }
}
