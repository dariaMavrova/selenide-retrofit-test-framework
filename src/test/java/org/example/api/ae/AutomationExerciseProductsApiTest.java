package org.example.api.ae;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.example.api.ApiClients;
import org.example.api.ae.model.Brand;
import org.example.api.ae.model.Product;
import org.example.api.ae.model.ProductsResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.api.ApiCalls.executeAndExpect;
import static org.example.api.ae.AeAssertions.METHOD_NOT_SUPPORTED;
import static org.example.api.ae.AeAssertions.assertResponse;

@Tag("api")
@Epic("AutomationExercise shop")
@Feature("Catalogue API")
@DisplayName("AutomationExercise API: products and brands")
class AutomationExerciseProductsApiTest {

    private final AutomationExerciseApi api = ApiClients.automationExercise();

    @Test
    @DisplayName("GET productsList returns a catalogue of valid products")
    void productsList() {
        ProductsResponse response = executeAndExpect(api.getProducts(), 200);

        assertThat(response.responseCode()).isEqualTo(200);
        assertThat(response.products())
                .isNotEmpty()
                .allSatisfy(product -> {
                    assertThat(product.name()).isNotBlank();
                    assertThat(product.price()).matches("Rs\\. \\d+");
                    assertThat(product.category().usertype().usertype()).isIn("Women", "Men", "Kids");
                });
        assertThat(response.products()).extracting(Product::id).doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("GET brandsList returns all brands used by products")
    void brandsCoverProducts() {
        List<String> brands = executeAndExpect(api.getBrands(), 200).brands().stream()
                .map(Brand::brand)
                .toList();
        List<String> productBrands = executeAndExpect(api.getProducts(), 200).products().stream()
                .map(Product::brand)
                .distinct()
                .toList();

        assertThat(brands).containsAll(productBrands);
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"top", "jean", "tshirt", "dress"})
    @DisplayName("POST searchProduct returns only matching products")
    void searchProduct(String query) {
        ProductsResponse response = executeAndExpect(api.searchProduct(query), 200);

        assertThat(response.responseCode()).isEqualTo(200);
        assertThat(response.products())
                .isNotEmpty()
                .allSatisfy(product -> assertThat(product.name() + " " + product.category().category())
                        .containsIgnoringCase(singular(query)));
    }

    @Test
    @DisplayName("POST searchProduct with an unknown term returns an empty list")
    void searchProductNoResults() {
        ProductsResponse response = executeAndExpect(api.searchProduct("no-such-product-xyz"), 200);

        assertThat(response.products()).isEmpty();
    }

    @Test
    @DisplayName("POST searchProduct without the parameter is a bad request")
    void searchProductMissingParameter() {
        assertResponse(api.searchProductWithoutQuery(), 400,
                "Bad request, search_product parameter is missing in POST request.");
    }

    @Test
    @DisplayName("Unsupported methods return 405")
    void unsupportedMethods() {
        assertResponse(api.postProducts(), 405, METHOD_NOT_SUPPORTED);
        assertResponse(api.putBrands(), 405, METHOD_NOT_SUPPORTED);
    }

    private static String singular(String query) {
        return query.endsWith("s") ? query.substring(0, query.length() - 1) : query;
    }
}
