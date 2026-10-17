package org.example.api.reqres;

import org.example.api.ApiClients;
import org.example.api.reqres.model.CreateUserRequest;
import org.example.api.reqres.model.CreateUserResponse;
import org.example.api.reqres.model.SingleUserResponse;
import org.example.api.reqres.model.UpdateUserResponse;
import org.example.api.reqres.model.User;
import org.example.api.reqres.model.UserListResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import retrofit2.Response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.api.ApiCalls.execute;
import static org.example.api.ApiCalls.executeAndExpect;

@Tag("api")
class ReqresUsersApiTest {

    private final ReqresApi api = ApiClients.reqres();

    @Test
    @DisplayName("GET /users/{id} returns the requested user")
    void getSingleUser() {
        SingleUserResponse response = executeAndExpect(api.getUser(2), 200);

        User user = response.data();
        assertThat(user.id()).isEqualTo(2);
        assertThat(user.email()).endsWith("@reqres.in");
        assertThat(user.firstName()).isNotBlank();
        assertThat(user.lastName()).isNotBlank();
    }

    @Test
    @DisplayName("GET /users/{id} returns 404 for a missing user")
    void getMissingUser() {
        Response<SingleUserResponse> response = execute(api.getUser(23));

        assertThat(response.code()).isEqualTo(404);
    }

    @ParameterizedTest(name = "page {0}")
    @ValueSource(ints = {1, 2})
    @DisplayName("GET /users?page returns a consistent page")
    void listUsersPagination(int page) {
        UserListResponse response = executeAndExpect(api.listUsers(page), 200);

        assertThat(response.page()).isEqualTo(page);
        assertThat(response.data())
                .hasSizeLessThanOrEqualTo(response.perPage())
                .isNotEmpty()
                .allSatisfy(user -> assertThat(user.email()).contains("@"));
        assertThat(response.data()).extracting(User::id).doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("POST /users creates a user")
    void createUser() {
        CreateUserRequest request = new CreateUserRequest("morpheus", "leader");

        CreateUserResponse response = executeAndExpect(api.createUser(request), 201);

        assertThat(response.id()).isNotBlank();
        assertThat(response.name()).isEqualTo(request.name());
        assertThat(response.job()).isEqualTo(request.job());
        assertThat(response.createdAt()).isNotBlank();
    }

    @Test
    @DisplayName("PUT /users/{id} updates a user")
    void updateUser() {
        CreateUserRequest request = new CreateUserRequest("morpheus", "zion resident");

        UpdateUserResponse response = executeAndExpect(api.updateUser(2, request), 200);

        assertThat(response.job()).isEqualTo("zion resident");
        assertThat(response.updatedAt()).isNotBlank();
    }

    @Test
    @DisplayName("DELETE /users/{id} returns 204")
    void deleteUser() {
        Response<Void> response = execute(api.deleteUser(2));

        assertThat(response.code()).isEqualTo(204);
    }
}
