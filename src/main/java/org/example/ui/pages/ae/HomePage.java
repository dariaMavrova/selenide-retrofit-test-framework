package org.example.ui.pages.ae;

import org.example.config.Config;
import io.qameta.allure.Step;
import com.codeborne.selenide.Selenide;

import static com.codeborne.selenide.Selenide.page;

public class HomePage {

    private final Header header = new Header();

    public Header header() {
        return header;
    }

    @Step("Open the home page")
    public static HomePage open() {
        Selenide.open(Config.aeUrl() + "/");
        return page(HomePage.class);
    }
}
