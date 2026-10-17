package org.example.ui.pages.ae;

import io.qameta.allure.Step;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.page;

/**
 * Top navigation bar, shared by every page of the site.
 */
public class Header {

    private static final String NAV_LINKS = "#header .navbar-nav li";

    public SelenideElement loggedInAs() {
        return $$(NAV_LINKS).findBy(text("Logged in as"));
    }

    public SelenideElement loginLink() {
        return $("#header a[href='/login']");
    }

    @Step("Open Products from the header")
    public ProductsPage openProducts() {
        $("#header a[href='/products']").click();
        return page(ProductsPage.class);
    }

    @Step("Open Cart from the header")
    public CartPage openCart() {
        $("#header a[href='/view_cart']").click();
        return page(CartPage.class);
    }

    @Step("Log out")
    public LoginSignupPage logout() {
        $("#header a[href='/logout']").click();
        return page(LoginSignupPage.class);
    }
}
