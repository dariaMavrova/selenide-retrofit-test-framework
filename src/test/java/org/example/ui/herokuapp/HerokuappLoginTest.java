package org.example.ui.herokuapp;

import org.example.config.Config;
import org.example.ui.BaseUiTest;
import org.example.ui.pages.herokuapp.LoginPage;
import org.example.ui.pages.herokuapp.SecureAreaPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static com.codeborne.selenide.Condition.cssClass;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.WebDriverConditions.urlContaining;
import static com.codeborne.selenide.Selenide.webdriver;

class HerokuappLoginTest extends BaseUiTest {

    private static final String VALID_USER = "tomsmith";
    private static final String VALID_PASSWORD = "SuperSecretPassword!";

    @Override
    protected String baseUrl() {
        return Config.herokuappUrl();
    }

    @Test
    @DisplayName("Valid credentials open the secure area")
    void successfulLogin() {
        SecureAreaPage secureArea = LoginPage.open().loginAs(VALID_USER, VALID_PASSWORD);

        webdriver().shouldHave(urlContaining("/secure"));
        secureArea.header().shouldHave(text("Secure Area"));
        secureArea.flashMessage()
                .shouldHave(cssClass("success"))
                .shouldHave(text("You logged into a secure area!"));
    }

    @Test
    @DisplayName("Logout returns to the login page")
    void logout() {
        LoginPage loginPage = LoginPage.open()
                .loginAs(VALID_USER, VALID_PASSWORD)
                .logout();

        webdriver().shouldHave(urlContaining("/login"));
        loginPage.flashMessage().shouldHave(text("You logged out of the secure area!"));
    }

    @ParameterizedTest(name = "[{index}] user={0} -> {2}")
    @CsvSource({
            "tomsmith, wrongPassword, Your password is invalid!",
            "unknown,  SuperSecretPassword!, Your username is invalid!",
            "'',       '',            Your username is invalid!"
    })
    @DisplayName("Invalid credentials show an error")
    void invalidLogin(String username, String password, String expectedError) {
        LoginPage loginPage = LoginPage.open().loginExpectingError(username, password);

        webdriver().shouldHave(urlContaining("/login"));
        loginPage.flashMessage()
                .shouldHave(cssClass("error"))
                .shouldHave(text(expectedError));
    }
}
