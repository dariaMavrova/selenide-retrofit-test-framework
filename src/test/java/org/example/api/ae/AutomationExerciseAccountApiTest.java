package org.example.api.ae;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import org.example.api.ApiClients;
import org.example.api.ae.model.Account;
import org.example.api.ae.model.UserDetailResponse;
import org.example.api.ae.model.UserDetailResponse.UserDetail;
import org.example.extensions.TestAccount;
import org.example.extensions.TestAccountExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.qameta.allure.SeverityLevel.BLOCKER;
import static io.qameta.allure.SeverityLevel.CRITICAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.example.api.ApiCalls.executeAndExpect;
import static org.example.api.ae.AeAssertions.METHOD_NOT_SUPPORTED;
import static org.example.api.ae.AeAssertions.assertResponse;

@Tag("api")
@Epic("AutomationExercise shop")
@Feature("Accounts API")
@DisplayName("AutomationExercise API: accounts")
@ExtendWith(TestAccountExtension.class)
class AutomationExerciseAccountApiTest {

    private final AutomationExerciseApi api = ApiClients.automationExercise();

    @Test
    @Severity(BLOCKER)
    @DisplayName("createAccount registers a new user")
    void createAccount(@TestAccount(registered = false) Account account) {
        assertResponse(api.createAccount(account.toFormFields()), 201, "User created!");
    }

    @Test
    @DisplayName("an e-mail can only be registered once")
    void duplicateEmail(@TestAccount Account account) {
        assertResponse(api.createAccount(account.toFormFields()), 400, "Email already exists!");
    }

    @Test
    @Severity(BLOCKER)
    @DisplayName("registered account can log in")
    void verifyLoginValid(@TestAccount Account account) {
        assertResponse(api.verifyLogin(account.email(), account.password()), 200, "User exists!");
    }

    @Test
    @Severity(CRITICAL)
    @DisplayName("wrong password is rejected")
    void verifyLoginWrongPassword(@TestAccount Account account) {
        assertResponse(api.verifyLogin(account.email(), "wrong-password"), 404, "User not found!");
    }

    @Test
    @DisplayName("missing e-mail is a bad request")
    void verifyLoginMissingEmail(@TestAccount Account account) {
        assertResponse(api.verifyLogin(null, account.password()), 400,
                "Bad request, email or password parameter is missing in POST request.");
    }

    @Test
    @DisplayName("DELETE verifyLogin is not supported")
    void verifyLoginWrongMethod() {
        assertResponse(api.deleteVerifyLogin(), 405, METHOD_NOT_SUPPORTED);
    }

    @Test
    @Severity(CRITICAL)
    @DisplayName("getUserDetailByEmail returns the data the account was created with")
    void userDetails(@TestAccount Account account) {
        UserDetailResponse response = executeAndExpect(api.getUserDetailByEmail(account.email()), 200);

        assertThat(response.responseCode()).isEqualTo(200);
        UserDetail user = response.user();
        assertSoftly(softly -> {
            softly.assertThat(user.name()).isEqualTo(account.name());
            softly.assertThat(user.email()).isEqualTo(account.email());
            softly.assertThat(user.firstName()).isEqualTo(account.firstName());
            softly.assertThat(user.lastName()).isEqualTo(account.lastName());
            softly.assertThat(user.country()).isEqualTo(account.country());
            softly.assertThat(user.city()).isEqualTo(account.city());
            softly.assertThat(user.zipcode()).isEqualTo(account.zipcode());
        });
    }

    @Test
    @DisplayName("updateAccount changes the stored details")
    void updateAccount(@TestAccount Account account) {
        Account updated = account.withName("Updated Name").withCity("Updated City");

        assertResponse(api.updateAccount(updated.toFormFields()), 200, "User updated!");

        UserDetail user = executeAndExpect(api.getUserDetailByEmail(account.email()), 200).user();
        assertThat(user.name()).isEqualTo("Updated Name");
        assertThat(user.city()).isEqualTo("Updated City");
    }

    @Test
    @Severity(CRITICAL)
    @DisplayName("deleted account can no longer log in")
    void deleteAccountRemovesUser(@TestAccount Account account) {
        assertResponse(api.deleteAccount(account.email(), account.password()), 200, "Account deleted!");

        assertResponse(api.verifyLogin(account.email(), account.password()), 404, "User not found!");
        assertThat(executeAndExpect(api.getUserDetailByEmail(account.email()), 200).responseCode())
                .isEqualTo(404);
    }
}
