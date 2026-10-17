package org.example.ui.pages.ae;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.page;

/**
 * /product_details/{id}
 */
public class ProductDetailsPage {

    private static final String INFO = ".product-information";

    public static ProductDetailsPage open(int productId) {
        Selenide.open("/product_details/" + productId);
        return page(ProductDetailsPage.class);
    }

    public SelenideElement name() {
        return $(INFO + " h2");
    }

    public SelenideElement category() {
        return $(INFO + " p");
    }

    public SelenideElement price() {
        return $(INFO + " span span");
    }

    public AddedToCartModal addToCart(int quantity) {
        $("#quantity").setValue(String.valueOf(quantity));
        $(INFO + " button.cart").click();
        return page(AddedToCartModal.class);
    }
}
