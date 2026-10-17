package org.example.ui.pages.ae;

import com.codeborne.selenide.Selenide;

import static com.codeborne.selenide.Selenide.page;

public class HomePage {

    public final Header header = new Header();

    public static HomePage open() {
        Selenide.open("/");
        return page(HomePage.class);
    }
}
