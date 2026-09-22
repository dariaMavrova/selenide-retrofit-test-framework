package org.example.ui;

import com.codeborne.selenide.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.closeWebDriver;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.Condition.text;

class HerokuappLoginTest {

    @BeforeEach
    void setUp() {
        Configuration.browser = "chrome";
        Configuration.headless = true;
        Configuration.timeout = 10000;
    }

    @AfterEach
    void tearDown() {
        closeWebDriver();
    }

    @Test
    void successfulLoginShowsFlashMessage() {
        open("https://the-internet.herokuapp.com/login");

        $("#username").setValue("tomsmith");
        $("#password").setValue("SuperSecretPassword!");
        $("button[type='submit']").click();

        $("#flash").shouldHave(text("You logged into a secure area"));
    }
}
