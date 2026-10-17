package org.example.ui.pages.ae;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.page;

public class AccountCreatedPage {

    public SelenideElement title() {
        return $("[data-qa='account-created']");
    }

    public HomePage continueToHome() {
        $("[data-qa='continue-button']").click();
        return page(HomePage.class);
    }
}
