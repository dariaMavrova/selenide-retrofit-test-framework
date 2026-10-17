package org.example.ui.pages.herokuapp;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.page;

/**
 * Page Object for https://the-internet.herokuapp.com/secure
 */
public class SecureAreaPage {

    private final SelenideElement header = $("h2");
    private final SelenideElement flashMessage = $("#flash");
    private final SelenideElement logoutButton = $("a[href='/logout']");

    public SelenideElement header() {
        return header;
    }

    public SelenideElement flashMessage() {
        return flashMessage;
    }

    @Step("Log out")
    public LoginPage logout() {
        logoutButton.click();
        return page(LoginPage.class);
    }
}
