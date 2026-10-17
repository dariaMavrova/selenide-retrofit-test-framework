package org.example.ui.pages.herokuapp;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;
import org.example.config.Config;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.SetValueOptions.withText;
import static com.codeborne.selenide.Selenide.page;

/**
 * Page Object for https://the-internet.herokuapp.com/login
 */
public class LoginPage {

    private final SelenideElement usernameInput = $("#username");
    private final SelenideElement passwordInput = $("#password");
    private final SelenideElement submitButton = $("button[type='submit']");
    private final SelenideElement flashMessage = $("#flash");

    @Step("Open the login page")
    public static LoginPage open() {
        Selenide.open(Config.herokuappUrl() + "/login");
        return page(LoginPage.class);
    }

    @Step("Log in as {username}")
    public SecureAreaPage loginAs(String username, String password) {
        submit(username, password);
        return page(SecureAreaPage.class);
    }

    @Step("Try to log in as {username}")
    public LoginPage loginExpectingError(String username, String password) {
        submit(username, password);
        return this;
    }

    public SelenideElement flashMessage() {
        return flashMessage;
    }

    private void submit(String username, String password) {
        usernameInput.setValue(username);
        passwordInput.setValue(withText(password).sensitive());
        submitButton.click();
    }
}
