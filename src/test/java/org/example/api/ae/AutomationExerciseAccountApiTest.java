package org.example.api.ae;

import org.example.api.ApiClients;
import org.example.api.ae.model.Account;
import org.example.api.ae.model.UserDetailResponse;
import org.example.api.ae.model.UserDetailResponse.UserDetail;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.api.ApiCalls.execute;
import static org.example.api.ApiCalls.executeAndExpect;
import static org.example.api.ae.AccountFactory.randomAccount;
import static org.example.api.ae.AutomationExerciseProductsApiTest.assertMessage;

@Tag("api")
@DisplayName("AutomationExercise API: accounts")
class AutomationExerciseAccountApiTest {

    private final AutomationExerciseApi api = ApiClients.automationExercise();
    private Account account;

    @BeforeEach
    void createAccount() {
        account = randomAccount();
        assertMessage(api.createAccount(account.toFormFields()), 201, "User created!");
    }

    @AfterEach
    void deleteAccount() {
        execute(api.deleteAccount(account.email(), account.password()));
    }

    @Test
    @DisplayName("created account can log in")
    void verifyLoginValid() {
        assertMessage(api.verifyLogin(account.email(), account.password()), 200, "User exists!");
    }

    @Test
    @DisplayName("wrong password is rejected")
    void verifyLoginWrongPassword() {
        assertMessage(api.verifyLogin(account.email(), "wrong-password"), 404, "User not found!");
    }

    @Test
    @DisplayName("missing e-mail is a bad request")
    void verifyLoginMissingEmail() {
        assertMessage(api.verifyLogin(null, account.password()), 400,
                "Bad request, email or password parameter is missing in POST request.");
    }

    @Test
    @DisplayName("DELETE verifyLogin is not supported")
    void verifyLoginWrongMethod() {
        assertMessage(api.deleteVerifyLogin(), 405, "This request method is not supported.");
    }

    @Test
    @DisplayName("getUserDetailByEmail returns the data the account was created with")
    void userDetails() {
        UserDetailResponse response = executeAndExpect(api.getUserDetailByEmail(account.email()), 200);

        assertThat(response.responseCode()).isEqualTo(200);
        UserDetail user = response.user();
        assertThat(user.name()).isEqualTo(account.name());
        assertThat(user.email()).isEqualTo(account.email());
        assertThat(user.firstName()).isEqualTo(account.firstName());
        assertThat(user.lastName()).isEqualTo(account.lastName());
        assertThat(user.country()).isEqualTo(account.country());
        assertThat(user.city()).isEqualTo(account.city());
        assertThat(user.zipcode()).isEqualTo(account.zipcode());
    }

    @Test
    @DisplayName("updateAccount changes the stored details")
    void updateAccount() {
        Account updated = account.withName("Updated Name").withCity("Updated City");

        assertMessage(api.updateAccount(updated.toFormFields()), 200, "User updated!");

        UserDetail user = executeAndExpect(api.getUserDetailByEmail(account.email()), 200).user();
        assertThat(user.name()).isEqualTo("Updated Name");
        assertThat(user.city()).isEqualTo("Updated City");
    }

    @Test
    @DisplayName("deleted account can no longer log in")
    void deleteAccountRemovesUser() {
        assertMessage(api.deleteAccount(account.email(), account.password()), 200, "Account deleted!");

        assertMessage(api.verifyLogin(account.email(), account.password()), 404, "User not found!");
        assertThat(executeAndExpect(api.getUserDetailByEmail(account.email()), 200).responseCode())
                .isEqualTo(404);
    }

    @Test
    @DisplayName("an e-mail can only be registered once")
    void duplicateEmail() {
        assertMessage(api.createAccount(account.toFormFields()), 400, "Email already exists!");
    }
}
