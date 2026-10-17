package org.example.api.reqres;

import org.example.api.ApiClients;
import org.example.api.RetrofitClient;
import org.example.api.reqres.model.ErrorResponse;
import org.example.api.reqres.model.LoginRequest;
import org.example.api.reqres.model.LoginResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import retrofit2.Response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.api.ApiCalls.errorBody;
import static org.example.api.ApiCalls.execute;
import static org.example.api.ApiCalls.executeAndExpect;

@Tag("api")
class ReqresLoginApiTest {

    private final ReqresApi api = ApiClients.reqres();

    @Test
    @DisplayName("POST /login with valid credentials returns a token")
    void loginSuccess() {
        LoginResponse response = executeAndExpect(
                api.login(new LoginRequest("eve.holt@reqres.in", "cityslicka")), 200);

        assertThat(response.token()).isNotBlank();
    }

    @Test
    @DisplayName("POST /login without password returns 400 with an error")
    void loginMissingPassword() throws Exception {
        Response<LoginResponse> response = execute(api.login(new LoginRequest("peter@klaven", null)));

        assertThat(response.code()).isEqualTo(400);
        ErrorResponse error = RetrofitClient.MAPPER.readValue(errorBody(response), ErrorResponse.class);
        assertThat(error.error()).isEqualTo("Missing password");
    }
}
