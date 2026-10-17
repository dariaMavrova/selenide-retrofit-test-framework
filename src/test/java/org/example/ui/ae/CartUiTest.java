package org.example.ui.ae;

import org.example.api.ApiClients;
import org.example.api.ae.model.Product;
import org.example.ui.pages.ae.CartPage;
import org.example.ui.pages.ae.CartPage.CartRow;
import org.example.ui.pages.ae.ProductDetailsPage;
import org.example.ui.pages.ae.ProductsPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.Condition.disappear;
import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static org.example.api.ApiCalls.executeAndExpect;

@DisplayName("AutomationExercise UI: cart")
class CartUiTest extends AutomationExerciseUiTest {

    private Map<Integer, Product> catalogue;

    @BeforeEach
    void loadCatalogue() {
        catalogue = executeAndExpect(ApiClients.automationExercise().getProducts(), 200).products().stream()
                .collect(Collectors.toMap(Product::id, Function.identity()));
    }

    @Test
    @DisplayName("Quantity chosen on the product page is used for the line total")
    void quantityAndTotal() {
        Product product = catalogue.get(1);
        int quantity = 4;

        CartPage cart = ProductDetailsPage.open(product.id())
                .addToCart(quantity)
                .viewCart();

        CartRow row = cart.row(product.id());
        row.name().shouldHave(exactText(product.name()));
        row.price().shouldHave(exactText(product.price()));
        row.quantity().shouldHave(exactText(String.valueOf(quantity)));
        row.total().shouldHave(exactText("Rs. " + product.priceValue() * quantity));
    }

    @Test
    @DisplayName("Several products can be added from the list and removed again")
    void addAndRemoveProducts() {
        Product first = catalogue.get(1);
        Product second = catalogue.get(2);

        ProductsPage products = ProductsPage.open();
        products.addToCart(first.id()).continueShopping();
        CartPage cart = products.addToCart(second.id()).viewCart();

        cart.rows().shouldHave(size(2));
        cart.row(first.id()).name().shouldHave(exactText(first.name()));
        cart.row(second.id()).name().shouldHave(exactText(second.name()));

        cart.row(first.id()).remove();
        cart.row(first.id()).root().should(disappear);
        cart.rows().shouldHave(size(1));

        cart.row(second.id()).remove();
        cart.emptyCartMessage().shouldBe(visible).shouldHave(text("Cart is empty!"));
    }
}
