package org.example.ui.pages.ae;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.disappear;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.page;

/**
 * "Added!" pop-up shown after adding a product to the cart.
 */
public class AddedToCartModal {

    private final SelenideElement modal = $("#cartModal");

    public SelenideElement self() {
        return modal;
    }

    public CartPage viewCart() {
        modal.shouldBe(visible).$("a[href='/view_cart']").click();
        return page(CartPage.class);
    }

    public void continueShopping() {
        modal.shouldBe(visible).$(".close-modal").click();
        modal.should(disappear);
    }
}
