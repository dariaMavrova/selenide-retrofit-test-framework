package org.example.api.ae;

import io.qameta.allure.Allure;
import org.example.api.ae.model.MessageResponse;
import retrofit2.Call;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.api.ApiCalls.executeAndExpect;

/**
 * AutomationExercise always answers HTTP 200; the real status and message are in the body.
 */
public final class AeAssertions {

    public static final String METHOD_NOT_SUPPORTED = "This request method is not supported.";

    private AeAssertions() {
    }

    public static void assertResponse(Call<MessageResponse> call, int expectedCode, String expectedMessage) {
        MessageResponse response = executeAndExpect(call, 200);
        Allure.step("Body has responseCode " + expectedCode + " and message \"" + expectedMessage + "\"", () -> {
            assertThat(response.responseCode()).as("responseCode in body").isEqualTo(expectedCode);
            assertThat(response.message()).as("message in body").isEqualTo(expectedMessage);
        });
    }
}
