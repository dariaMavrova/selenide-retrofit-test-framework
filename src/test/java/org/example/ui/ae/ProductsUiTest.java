package org.example.ui.ae;

import io.qameta.allure.Feature;
import org.example.api.ApiClients;
import org.example.api.ae.AutomationExerciseApi;
import org.example.api.ae.model.Product;
import org.example.ui.pages.ae.ProductDetailsPage;
import org.example.ui.pages.ae.ProductsPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static com.codeborne.selenide.CollectionCondition.exactTexts;
import static com.codeborne.selenide.Condition.exactText;
import static com.codeborne.selenide.Condition.text;
import static org.example.api.ApiCalls.executeAndExpect;

@Feature("Catalogue")
@DisplayName("AutomationExercise UI: catalogue")
class ProductsUiTest extends AutomationExerciseUiTest {

    private final AutomationExerciseApi api = ApiClients.automationExercise();

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"top", "jeans", "saree"})
    @DisplayName("UI search shows exactly the products the API returns")
    void searchMatchesApi(String query) {
        List<String> expected = executeAndExpect(api.searchProduct(query), 200).products().stream()
                .map(Product::name)
                .toList();

        ProductsPage products = ProductsPage.open().search(query);

        products.title().shouldHave(exactText("Searched Products"));
        products.productNames().shouldHave(exactTexts(expected));
    }

    @ParameterizedTest(name = "product {0}")
    @ValueSource(ints = {1, 2, 5})
    @DisplayName("Product page shows the same data as the API")
    void productDetailsMatchApi(int productId) {
        Product expected = executeAndExpect(api.getProducts(), 200).products().stream()
                .filter(p -> p.id() == productId)
                .findFirst()
                .orElseThrow();

        ProductDetailsPage details = ProductDetailsPage.open(productId);

        details.name().shouldHave(exactText(expected.name()));
        details.price().shouldHave(exactText(expected.price()));
        details.category().shouldHave(text(
                expected.category().usertype().usertype() + " > " + expected.category().category()));
    }
}
