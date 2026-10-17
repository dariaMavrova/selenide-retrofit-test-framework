package org.example.ui.pages.herokuapp;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.page;

/**
 * Page Object for https://the-internet.herokuapp.com/login
 */
public class LoginPage {

    private final SelenideElement usernameInput = $("#username");
    private final SelenideElement passwordInput = $("#password");
    private final SelenideElement submitButton = $("button[type='submit']");
    private final SelenideElement flashMessage = $("#flash");

    public static LoginPage open() {
        com.codeborne.selenide.Selenide.open("/login");
        return page(LoginPage.class);
    }

    public SecureAreaPage loginAs(String username, String password) {
        submit(username, password);
        return page(SecureAreaPage.class);
    }

    public LoginPage loginExpectingError(String username, String password) {
        submit(username, password);
        return this;
    }

    public SelenideElement flashMessage() {
        return flashMessage;
    }

    private void submit(String username, String password) {
        usernameInput.setValue(username);
        passwordInput.setValue(password);
        submitButton.click();
    }
}
