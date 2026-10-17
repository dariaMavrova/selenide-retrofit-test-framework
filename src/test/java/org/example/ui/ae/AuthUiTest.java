package org.example.ui.ae;

import org.example.api.ApiClients;
import org.example.api.ae.AutomationExerciseApi;
import org.example.api.ae.model.Account;
import org.example.api.ae.model.UserDetailResponse.UserDetail;
import org.example.ui.pages.ae.HomePage;
import org.example.ui.pages.ae.LoginSignupPage;
import org.example.ui.pages.ae.SignupPage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Condition.disabled;
import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.value;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.webdriver;
import static com.codeborne.selenide.WebDriverConditions.urlContaining;
import static org.assertj.core.api.Assertions.assertThat;
import static org.example.api.ApiCalls.execute;
import static org.example.api.ApiCalls.executeAndExpect;
import static org.example.api.ae.AccountFactory.randomAccount;

@DisplayName("AutomationExercise UI: sign-up and login")
class AuthUiTest extends AutomationExerciseUiTest {

    private final AutomationExerciseApi api = ApiClients.automationExercise();
    private final Account account = randomAccount();

    @AfterEach
    void deleteAccountViaApi() {
        execute(api.deleteAccount(account.email(), account.password()));
    }

    @Test
    @DisplayName("New user registers through the UI; the account is stored with the entered data")
    void registerThroughUi() {
        SignupPage signup = LoginSignupPage.open().startSignup(account.name(), account.email());

        signup.name().shouldHave(value(account.name()));
        signup.email().shouldHave(value(account.email())).shouldBe(disabled);

        HomePage home = signup.fillAndSubmit(account)
                .continueToHome();
        home.header.loggedInAs().shouldHave(text("Logged in as " + account.name()));

        // Check the backend, not just the UI.
        UserDetail stored = executeAndExpect(api.getUserDetailByEmail(account.email()), 200).user();
        assertThat(stored.name()).isEqualTo(account.name());
        assertThat(stored.firstName()).isEqualTo(account.firstName());
        assertThat(stored.lastName()).isEqualTo(account.lastName());
        assertThat(stored.country()).isEqualTo(account.country());
        assertThat(stored.city()).isEqualTo(account.city());
        assertThat(stored.birthYear()).isEqualTo(String.valueOf(account.birthYear()));
    }

    @Test
    @DisplayName("User created via API logs in and out through the UI")
    void loginAndLogout() {
        createAccountViaApi();

        HomePage home = LoginSignupPage.open().loginAs(account.email(), account.password());
        home.header.loggedInAs().shouldHave(text("Logged in as " + account.name()));

        LoginSignupPage loginPage = home.header.logout();
        webdriver().shouldHave(urlContaining("/login"));
        loginPage.header.loginLink().shouldBe(visible);
        loginPage.header.loggedInAs().shouldNot(visible);
    }

    @Test
    @DisplayName("Wrong password shows an error and does not log in")
    void loginWithWrongPassword() {
        createAccountViaApi();

        LoginSignupPage loginPage = LoginSignupPage.open().loginExpectingError(account.email(), "wrong-password");

        loginPage.loginError().shouldHave(exactText("Your email or password is incorrect!"));
        loginPage.header.loggedInAs().shouldNot(visible);
    }

    @Test
    @DisplayName("Signing up with an existing e-mail is rejected")
    void signupWithExistingEmail() {
        createAccountViaApi();

        LoginSignupPage loginPage = LoginSignupPage.open()
                .startSignupExpectingError("Someone Else", account.email());

        loginPage.signupError().shouldHave(exactText("Email Address already exist!"));
    }

    private void createAccountViaApi() {
        assertThat(executeAndExpect(api.createAccount(account.toFormFields()), 200).responseCode())
                .as("account created via API")
                .isEqualTo(201);
    }
}
