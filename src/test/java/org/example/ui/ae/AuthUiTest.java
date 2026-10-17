package org.example.ui.ae;

import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import org.example.api.ApiClients;
import org.example.api.ae.model.Account;
import org.example.api.ae.model.UserDetailResponse.UserDetail;
import org.example.extensions.TestAccount;
import org.example.extensions.TestAccountExtension;
import org.example.ui.pages.ae.HomePage;
import org.example.ui.pages.ae.LoginSignupPage;
import org.example.ui.pages.ae.SignupPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.codeborne.selenide.Condition.disabled;
import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.value;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.webdriver;
import static com.codeborne.selenide.WebDriverConditions.urlContaining;
import static io.qameta.allure.SeverityLevel.BLOCKER;
import static io.qameta.allure.SeverityLevel.CRITICAL;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.example.api.ApiCalls.executeAndExpect;

@Feature("Sign-up and login")
@DisplayName("AutomationExercise UI: sign-up and login")
@ExtendWith(TestAccountExtension.class)
class AuthUiTest extends AutomationExerciseUiTest {

    @Test
    @Severity(BLOCKER)
    @DisplayName("New user registers through the UI; the account is stored with the entered data")
    void registerThroughUi(@TestAccount(registered = false) Account account) {
        SignupPage signup = LoginSignupPage.open().startSignup(account.name(), account.email());

        signup.name().shouldHave(value(account.name()));
        signup.email().shouldHave(value(account.email())).shouldBe(disabled);

        HomePage home = signup.fillAndSubmit(account).continueToHome();
        home.header().loggedInAs().shouldHave(text("Logged in as " + account.name()));

        // The UI says "created" - check that the backend really stored what was entered.
        UserDetail stored = executeAndExpect(
                ApiClients.automationExercise().getUserDetailByEmail(account.email()), 200).user();
        assertSoftly(softly -> {
            softly.assertThat(stored.name()).isEqualTo(account.name());
            softly.assertThat(stored.firstName()).isEqualTo(account.firstName());
            softly.assertThat(stored.lastName()).isEqualTo(account.lastName());
            softly.assertThat(stored.country()).isEqualTo(account.country());
            softly.assertThat(stored.city()).isEqualTo(account.city());
            softly.assertThat(stored.birthYear()).isEqualTo(String.valueOf(account.birthYear()));
        });
    }

    @Test
    @Severity(BLOCKER)
    @DisplayName("User registered via API logs in and out through the UI")
    void loginAndLogout(@TestAccount Account account) {
        HomePage home = LoginSignupPage.open().loginAs(account.email(), account.password());
        home.header().loggedInAs().shouldHave(text("Logged in as " + account.name()));

        LoginSignupPage loginPage = home.header().logout();
        webdriver().shouldHave(urlContaining("/login"));
        loginPage.header().loginLink().shouldBe(visible);
        loginPage.header().loggedInAs().shouldNot(visible);
    }

    @Test
    @Severity(CRITICAL)
    @DisplayName("Wrong password shows an error and does not log in")
    void loginWithWrongPassword(@TestAccount Account account) {
        LoginSignupPage loginPage = LoginSignupPage.open().loginExpectingError(account.email(), "wrong-password");

        loginPage.loginError().shouldHave(exactText("Your email or password is incorrect!"));
        loginPage.header().loggedInAs().shouldNot(visible);
    }

    @Test
    @DisplayName("Signing up with an existing e-mail is rejected")
    void signupWithExistingEmail(@TestAccount Account account) {
        LoginSignupPage loginPage = LoginSignupPage.open()
                .startSignupExpectingError("Someone Else", account.email());

        loginPage.signupError().shouldHave(exactText("Email Address already exist!"));
    }
}
