package org.example.api;

import org.example.api.ae.AutomationExerciseApi;
import org.example.api.booker.RestfulBookerApi;
import org.example.api.reqres.ReqresApi;
import org.example.config.Config;

import static org.example.api.RetrofitClient.header;

/**
 * Entry point for tests: one factory method per API under test.
 */
public final class ApiClients {

    private ApiClients() {
    }

    public static ReqresApi reqres() {
        return RetrofitClient.create(Config.reqresUrl(), ReqresApi.class,
                header("x-api-key", Config.reqresApiKey()));
    }

    public static RestfulBookerApi restfulBooker() {
        // GET /booking/{id} answers 418 unless the client explicitly accepts JSON.
        return RetrofitClient.create(Config.bookerUrl(), RestfulBookerApi.class,
                header("Accept", "application/json"));
    }

    public static AutomationExerciseApi automationExercise() {
        return RetrofitClient.create(Config.aeApiUrl(), AutomationExerciseApi.class);
    }
}
