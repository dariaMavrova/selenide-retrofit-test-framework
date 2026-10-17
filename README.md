# Test Automation Framework — Selenide + Retrofit

![Tests](https://github.com/dariaMavrova/selenide-retrofit-test-framework/actions/workflows/tests.yml/badge.svg)

A Java test automation framework for **UI** and **API** testing of real public applications,
with **50+ automated tests** running in GitHub Actions.

| Application | What is tested | Layer |
|---|---|---|
| [automationexercise.com](https://automationexercise.com) — e-commerce shop | sign-up, login/logout, search, product pages, cart; full account lifecycle via API | UI + API |
| [restful-booker](https://restful-booker.herokuapp.com/apidoc/index.html) — hotel booking API | token auth, booking CRUD, filtering, authorization rules | API |
| [reqres.in](https://reqres.in) — users API | CRUD, pagination, error responses | API |
| [the-internet.herokuapp.com](https://the-internet.herokuapp.com/login) | login form, negative cases | UI |

## Highlights

- **UI and API in one test.** Test data is created through the API, the flow is exercised in the browser,
  and the result is checked in the backend again. For example, `registerThroughUi` signs up in the UI and then
  checks the stored account through `getUserDetailByEmail`. UI search results are compared one-to-one with the API.
- **Typed API layer with Retrofit.** Each API is a Java interface (`ReqresApi`, `RestfulBookerApi`,
  `AutomationExerciseApi`), and payloads are Java records. Tests never touch raw JSON.
- **Handles real-world API quirks:** form-encoded bodies, a `DELETE` with a body, auth through a `Cookie` header,
  `418` without `Accept: application/json`, and an API that always answers HTTP 200 with the real status in
  `responseCode`.
- **Independent, self-cleaning tests.** Every test generates its own unique data (Datafaker factories)
  and deletes it afterwards, so tests can run in any order and in parallel.
- **Stable UI tests.** The shop serves full-screen Google "vignette" ads that hijack clicks. Chrome is started
  with `--host-resolver-rules` that block the ad domains, which removes the classic source of flakiness
  without any `Thread.sleep` or retries.
- **Page Object pattern** with fluent navigation (`loginAs()` returns `HomePage`, `viewCart()` returns `CartPage`)
  and reusable components (`Header`, `AddedToCartModal`, `CartRow`).
- **Data-driven tests** with JUnit 5 `@ParameterizedTest`, `@CsvSource` and `@ValueSource`; `@Nested` groups for readability.
- **CI**: GitHub Actions runs the API and UI suites as parallel jobs and uploads HTML reports and failure screenshots.

## Tech stack

| Area | Tools |
|---|---|
| Language | Java 21 |
| Build | Gradle (version catalog, Java toolchain) |
| Test runner | JUnit 5 |
| UI | Selenide, headless Chrome |
| API | Retrofit 2, OkHttp 4 (interceptors, logging), Jackson |
| Test data | Datafaker |
| Assertions | AssertJ, Selenide conditions |
| CI | GitHub Actions |

## Project structure

```
src
├── main/java/org/example                 # the framework: reusable, no tests here
│   ├── config/Config.java                # URLs and credentials, overridable with -D properties
│   ├── api
│   │   ├── RetrofitClient.java           # shared OkHttp client, logging (secrets redacted), JSON mapper
│   │   ├── ApiClients.java               # one factory method per API under test
│   │   ├── ApiCalls.java                 # execute calls + status-code assertions
│   │   ├── ae/                           # AutomationExercise: API interface, models, AccountFactory
│   │   ├── booker/                       # Restful-booker: API interface, models, BookingFactory
│   │   └── reqres/                       # Reqres: API interface, models
│   └── ui/pages
│       ├── ae/                           # Header, LoginSignupPage, SignupPage, ProductsPage, CartPage, ...
│       └── herokuapp/                    # LoginPage, SecureAreaPage
└── test/java/org/example
    ├── api/{ae,booker,reqres}            # @Tag("api")
    └── ui
        ├── BaseUiTest.java               # browser setup / teardown
        ├── ae/                           # @Tag("ui"): auth, catalogue, cart
        └── herokuapp/
```

## Running the tests

Needs JDK 17+ to launch Gradle (Gradle provisions Java 21 itself) and Google Chrome for the UI tests.

```bash
./gradlew test       # everything
./gradlew apiTest    # API tests only
./gradlew uiTest     # UI tests only
```

Options:

```bash
./gradlew uiTest -Dselenide.headless=false             # watch the browser
./gradlew uiTest -Dselenide.browser=firefox            # another browser
./gradlew apiTest -Dapi.booker.url=https://my-env/     # point an API at another environment
```

Reports end up in `build/reports/tests/`. Screenshots and page sources of failed UI tests go to `build/reports/selenide/`.
