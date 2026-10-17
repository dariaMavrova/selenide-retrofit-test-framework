package org.example.ui.pages.ae;

import org.example.config.Config;
import io.qameta.allure.Step;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.disappear;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.page;

/**
 * /view_cart
 */
public class CartPage {

    private final Header header = new Header();

    public Header header() {
        return header;
    }

    @Step("Open the cart")
    public static CartPage open() {
        Selenide.open(Config.aeUrl() + "/view_cart");
        return page(CartPage.class);
    }

    public ElementsCollection rows() {
        return $$("#cart_info_table tbody tr");
    }

    public CartRow row(int productId) {
        return new CartRow($("#product-" + productId));
    }

    public SelenideElement emptyCartMessage() {
        return $("#empty_cart");
    }

    public record CartRow(SelenideElement root) {

        public SelenideElement name() {
            return root.$(".cart_description h4 a");
        }

        public SelenideElement price() {
            return root.$(".cart_price p");
        }

        public SelenideElement quantity() {
            return root.$(".cart_quantity button");
        }

        public SelenideElement total() {
            return root.$(".cart_total_price");
        }

        /** Waits until the row is gone: the site removes it only after its AJAX call succeeds. */
        @Step("Remove the product from the cart")
        public void remove() {
            root.$(".cart_quantity_delete").click();
            root.should(disappear);
        }
    }
}
