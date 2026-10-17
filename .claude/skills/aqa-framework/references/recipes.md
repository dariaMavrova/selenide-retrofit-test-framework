# Recipes

Templates for each common change, written the way the existing code is. Adapt names; keep the shape.

- [New endpoint on an existing API](#new-endpoint-on-an-existing-api)
- [New API (new service)](#new-api-new-service)
- [API test](#api-test)
- [New page object](#new-page-object)
- [UI test](#ui-test)
- [New fixture extension](#new-fixture-extension)
- [New site for UI tests](#new-site-for-ui-tests)

## New endpoint on an existing API

1. Probe it: `curl -s -X POST -d field=value https://automationexercise.com/api/<endpoint>`. Note the status, body shape and error cases.
2. Add the method to the interface (e.g. `api/ae/AutomationExerciseApi.java`), with a Javadoc comment if anything is unusual:

```java
/** A null field is left out of the request body. */
@FormUrlEncoded
@POST("verifyLogin")
Call<MessageResponse> verifyLogin(@Field("email") String email, @Field("password") String password);
```

3. Add a model record in `model/` if the response is new:

```java
public record BrandsResponse(int responseCode, List<Brand> brands) {
}
```

Use `@JsonProperty("snake_case")` for fields whose JSON name differs, and nest small types as inner records (see `Product`).

## New API (new service)

1. Package `org.example.api.<name>` with `<Name>Api.java` (Retrofit interface) and `model/`.
2. URL and credentials in `Config`:

```java
public static String fooUrl() {
    return get("api.foo.url", "https://foo.example.com/api/");   // trailing slash: Retrofit base URLs need it
}
```

3. A factory method in `ApiClients`, with API-wide headers as interceptors:

```java
public static FooApi foo() {
    return RetrofitClient.create(Config.fooUrl(), FooApi.class,
            header("Accept", "application/json"));
}
```

4. Random test data goes in a `<Thing>Factory` with a per-thread Faker (copy `BookingFactory`).
5. Add the API to the README's application table.

## API test

```java
@Tag("api")
@Epic("Restful-booker")
@Feature("Bookings API")
@DisplayName("Restful-booker API")
class RestfulBookerApiTest {

    private final RestfulBookerApi api = ApiClients.restfulBooker();

    @Test
    @Severity(CRITICAL)
    @DisplayName("PUT without a token is forbidden and changes nothing")
    void updateWithoutToken() {
        // arrange: own data, created through the API
        // act + assert: check the status AND that nothing changed
        assertThat(execute(api.updateBooking(bookingId, randomBooking(), null)).code()).isEqualTo(403);
        assertThat(executeAndExpect(api.getBooking(bookingId), 200)).isEqualTo(booking);
    }
}
```

- `executeAndExpect(call, status)` when you want the body; `execute(call)` when the test is about the status itself.
- For AutomationExercise (always HTTP 200): `assertResponse(call, expectedResponseCode, expectedMessage)`.
- Group related tests with `@Nested` classes that each have their own `@BeforeEach` (see `RestfulBookerApiTest.Crud`).
- Cleanup in `@AfterEach` (best effort, ignore "already gone") or through an extension.

## New page object

`src/main/java/org/example/ui/pages/<site>/<Name>Page.java`:

```java
/**
 * /products: full catalogue and search results.
 */
public class ProductsPage {

    private final Header header = new Header();

    private final SelenideElement searchInput = $("#search_product");
    private final SelenideElement searchButton = $("#submit_search");

    public Header header() {
        return header;
    }

    @Step("Open the Products page")
    public static ProductsPage open() {
        Selenide.open(Config.aeUrl() + "/products");        // absolute URL, never relative (rule 5.5)
        return page(ProductsPage.class);
    }

    @Step("Search for \"{query}\"")
    public ProductsPage search(String query) {                 // stays on the page: returns this
        searchInput.setValue(query);
        searchButton.click();
        return this;
    }

    @Step("Add product {productId} to the cart from the list")
    public AddedToCartModal addToCart(int productId) {         // leads somewhere else: returns that page/component
        $(".features_items .productinfo a.add-to-cart[data-product-id='" + productId + "']").click();
        return page(AddedToCartModal.class);
    }

    public ElementsCollection productNames() {                 // elements for the test to assert on
        return $$(".features_items .productinfo p");
    }
}
```

- Get selectors from the real page: `curl -s <url> | grep -oE 'data-qa="[^"]+"' | sort -u`.
- AJAX action: wait for its effect inside the method (`root.should(disappear)` in `CartRow.remove()`).
- Passwords: `setValue(withText(password).sensitive())`.
- Repeated parts of the page are components (`Header`) or small records wrapping a root element (`CartPage.CartRow`).

## UI test

```java
@Feature("Sign-up and login")
@DisplayName("AutomationExercise UI: sign-up and login")
@ExtendWith(TestAccountExtension.class)
class AuthUiTest extends AutomationExerciseUiTest {           // site base class: @Epic; BaseUiTest: browser + @Tag("ui")

    @Test
    @Severity(BLOCKER)
    @DisplayName("User registered via API logs in and out through the UI")
    void loginAndLogout(@TestAccount Account account) {       // precondition via API, cleaned up automatically
        HomePage home = LoginSignupPage.open().loginAs(account.email(), account.password());
        home.header().loggedInAs().shouldHave(text("Logged in as " + account.name()));

        LoginSignupPage loginPage = home.header().logout();
        webdriver().shouldHave(urlContaining("/login"));
        loginPage.header().loggedInAs().shouldNot(visible);
    }
}
```

- No selectors and no `getText()` in tests. Assert with Selenide conditions on elements the page object returns.
- Compare against the API where possible: `products.productNames().shouldHave(exactTexts(namesFromApi))`.
- After a UI action that stores data, verify it through the API (`AuthUiTest.registerThroughUi`).

## New fixture extension

When several tests need the same precondition, copy `extensions/TestAccount.java` + `TestAccountExtension.java`:

- An annotation on the parameter (`@Target(PARAMETER)`), with options as attributes (`registered = false`).
- `ParameterResolver.resolveParameter`: create the data through the API inside `Allure.step("Precondition: ...")`,
  and store it in `context.getStore(NAMESPACE)` so each test keeps its own (parallel-safe).
- `AfterEachCallback.afterEach`: delete everything stored, inside `Allure.step("Cleanup: ...")`. Never fail on "already deleted".

## New site for UI tests

1. URL in `Config` (`ui.<site>.url`).
2. Page objects in `ui/pages/<site>/`.
3. A base class in `src/test/java/org/example/ui/<site>/` that carries the site's `@Epic`:

```java
@Epic("AutomationExercise shop")
abstract class AutomationExerciseUiTest extends BaseUiTest {
}
```

4. If the site has ads or trackers that interfere, add their domains to `BLOCKED_HOSTS` in `BaseUiTest`.
5. Run the new tests several times in the full parallel suite, then update the README.
