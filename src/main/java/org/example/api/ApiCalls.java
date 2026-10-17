package org.example.api;

import io.qameta.allure.Allure;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Executes Retrofit calls synchronously, each as an Allure step named after the request.
 */
public final class ApiCalls {

    private ApiCalls() {
    }

    public static <T> Response<T> execute(Call<T> call) {
        var request = call.request();
        return Allure.step(request.method() + " " + request.url().encodedPath(), () -> {
            try {
                return call.execute();
            } catch (IOException e) {
                throw new UncheckedIOException("Request failed: " + describe(request), e);
            }
        });
    }

    /** Executes the call, asserts the expected HTTP status and returns the body. */
    public static <T> T executeAndExpect(Call<T> call, int expectedStatus) {
        Response<T> response = execute(call);
        if (response.code() != expectedStatus) {
            throw new AssertionError("Expected HTTP " + expectedStatus + " but got " + response.code()
                    + " for " + describe(response.raw().request()) + "\nBody: " + errorBody(response));
        }
        return response.body();
    }

    /** Method and URL only: headers may carry tokens. */
    private static String describe(okhttp3.Request request) {
        return request.method() + " " + request.url();
    }

    public static String errorBody(Response<?> response) {
        try (var body = response.errorBody()) {
            return body == null ? "" : body.string();
        } catch (IOException e) {
            return "<unreadable: " + e.getMessage() + ">";
        }
    }
}
