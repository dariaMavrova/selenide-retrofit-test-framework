package org.example.ui.pages.ae;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.page;

/**
 * /products: full catalogue and search results.
 */
public class ProductsPage {

    public final Header header = new Header();

    private final SelenideElement searchInput = $("#search_product");
    private final SelenideElement searchButton = $("#submit_search");

    public static ProductsPage open() {
        Selenide.open("/products");
        return page(ProductsPage.class);
    }

    public ProductsPage search(String query) {
        searchInput.setValue(query);
        searchButton.click();
        return this;
    }

    public SelenideElement title() {
        return $(".features_items h2.title");
    }

    public ElementsCollection productNames() {
        return $$(".features_items .productinfo p");
    }

    public AddedToCartModal addToCart(int productId) {
        $(".features_items .productinfo a.add-to-cart[data-product-id='" + productId + "']").click();
        return page(AddedToCartModal.class);
    }
}
