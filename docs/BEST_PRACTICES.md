# Test Automation Best Practices

The rules this framework follows, and the checklist for every code review.
Each rule says **what** to do, **why** it matters, and **how to check** it in a review.

**Severity** in reviews:
- 🔴 **Blocker**: the tests give wrong answers (false pass or false fail), leak secrets, or break other tests. Must fix before merge.
- 🟠 **Major**: a flaky, slow or hard-to-maintain test, or a broken framework convention. Fix before merge unless there is a reason.
- 🟡 **Minor**: readability, naming, small duplication. Fix when convenient.

Contents:
[1. Test design](#1-test-design) ·
[2. Test data and fixtures](#2-test-data-and-fixtures) ·
[3. Assertions](#3-assertions) ·
[4. API layer (Retrofit)](#4-api-layer-retrofit) ·
[5. UI layer (Selenide, Page Objects)](#5-ui-layer-selenide-page-objects) ·
[6. Parallel execution and thread safety](#6-parallel-execution-and-thread-safety) ·
[7. Flakiness](#7-flakiness) ·
[8. Reporting (Allure)](#8-reporting-allure) ·
[9. Configuration and secrets](#9-configuration-and-secrets) ·
[10. Build, dependencies and CI](#10-build-dependencies-and-ci) ·
[11. Code style](#11-code-style) ·
[Review checklist](#review-checklist)

---

## 1. Test design

**1.1 One behaviour per test. The name says what is expected.** 🟠
A failing test should tell you what broke without reading its code.
Check: `@DisplayName` reads as an expectation, e.g. `"Wrong password shows an error and does not log in"`,
not `"test login 2"`. If a test checks unrelated behaviours, split it.

**1.2 Tests are independent: any order, any subset, in parallel.** 🔴
A test that relies on another test's data or browser state passes or fails depending on what ran before it.
Check: no shared mutable fields between tests, no `@TestMethodOrder` used to chain tests, and every test creates the data it needs.

**1.3 Set up through the API, act through the UI, verify where it matters.** 🟠
UI setup is slow and flaky, and it tests things the test is not about. The UI is for the user flow under test.
After a UI action that changes data, verify the backend too: the UI saying "created" does not mean it was stored.
Check: preconditions such as "a registered user" come from `@TestAccount` or another API call, not from clicking through sign-up.
Examples: `AuthUiTest.registerThroughUi`, `ProductsUiTest.searchMatchesApi`.

**1.4 Cover negative and edge cases, not only the happy path.** 🟡
Most real bugs live in validation and error handling.
Check: each feature has at least one negative test (wrong input, missing field, unsupported method, no permission).

**1.5 Data-driven tests for the same behaviour with different inputs.** 🟡
Use `@ParameterizedTest` with `@ValueSource` or `@CsvSource` instead of copy-pasted tests.
Give it a readable `name`: `@ParameterizedTest(name = "user={0} -> {2}")`.
JUnit 6 already quotes string arguments, so don't add quotes in the pattern.

**1.6 Every test class has a `@Tag` (`api` or `ui`).** 🟠
CI and `./gradlew apiTest` / `uiTest` select tests by tag, so an untagged test never runs in CI.
UI tests inherit `@Tag("ui")` from `BaseUiTest`.

## 2. Test data and fixtures

**2.1 Generate unique data per test. Never hard-code shared accounts or IDs.** 🔴
Shared data collides in parallel runs and breaks when someone else changes it.
Check: use `AccountFactory.randomAccount()` / `BookingFactory.randomBooking()`. E-mails contain a UUID.
Fixed values are fine only for public read-only data, such as product IDs in the catalogue.

**2.2 Every test cleans up what it created, even when it fails.** 🟠
Leftover data slows down shared environments and can change other tests' results.
Check: cleanup goes in an extension (`TestAccountExtension.afterEach`) or `@AfterEach`, never at the end of the test body,
where it is skipped when an assertion fails. Cleanup must not fail the test if the data is already gone.

**2.3 Reusable preconditions are JUnit 5 extensions, not base-class methods.** 🟡
Extensions compose (`@ExtendWith` several), are explicit in the test signature, and keep state in the JUnit store of each test.
Pattern: `void loginAndLogout(@TestAccount Account account)`; use `@TestAccount(registered = false)` for data only.

**2.4 Models are immutable records. Variations use `with...()` copies.** 🟡
Example: `account.withName("Updated Name")`, `booking.withTotalprice(999)`.

## 3. Assertions

**3.1 Assertions live in tests, not in page objects or API clients.** 🟠
Page objects describe the page, and the test decides what is expected. Page objects return `SelenideElement`s
or values. Selenide `should...` waits inside actions are fine; they are synchronisation, not test expectations.
Check: no `assertThat` in `src/main`.

**3.2 Use AssertJ for values and Selenide conditions for the page.** 🟡
`assertThat(...)` for API responses and models; `element.shouldHave(exactText(...))` for UI, because it waits.
Never `assertThat(element.getText())` for UI: it reads once and does not wait, which makes the test flaky.

**3.3 Check the real status, not just "something came back".** 🔴
- Retrofit: use `executeAndExpect(call, expectedStatus)`; it fails with method, URL and body.
- AutomationExercise always answers HTTP 200, with the real status in `responseCode`: use `AeAssertions.assertResponse`.
- Restful-booker answers 200 with `reason: "Bad credentials"` on a failed login, so check the body, not only the code.

**3.4 Soft assertions when checking several fields of one object.** 🟡
`assertSoftly(...)` reports every mismatch at once instead of stopping at the first.

**3.5 Prefer exact, specific matchers.** 🟡
`exactText` over `text` when the full text is known; `isEqualTo(expected)` on whole records over field-by-field comparison;
`exactTexts(list)` to compare UI lists with API results. Weak matchers such as `isNotNull()` alone hide bugs.

## 4. API layer (Retrofit)

**4.1 One Retrofit interface per API, one method per endpoint.** 🟠
The interface is the contract: HTTP method, path and parameters are visible in one place.
New API: package `org.example.api.<name>` with the interface plus `model/`, and a factory method in `ApiClients`.

**4.2 Payloads are typed records. Tests don't parse raw JSON.** 🟠
Use `@JsonProperty` for snake_case fields. The shared `ObjectMapper` ignores unknown fields and handles `java.time`.

**4.3 Cross-cutting concerns are OkHttp interceptors added in `ApiClients`, not repeated per call.** 🟡
Auth headers, `Accept` headers and logging go in interceptors. Per-request credentials such as the booker token cookie
are explicit `@Header` parameters, so tests can also send no token to check authorization.

**4.4 Know Retrofit's edge cases.** 🟠
- A form body on DELETE needs `@HTTP(method = "DELETE", path = "...", hasBody = true)`.
- `@FormUrlEncoded` for form APIs; `@Field` with a null value is left out (useful for "missing parameter" tests).
- `Call<Void>` when the response body is irrelevant.

**4.5 Execute calls only through `ApiCalls`.** 🟡
It makes every request an Allure step and gives clear failure messages without leaking headers.

## 5. UI layer (Selenide, Page Objects)

**5.1 Page objects expose user actions and elements, never WebDriver details.** 🟠
Actions are methods that read like user intent (`loginAs`, `addToCart`, `search`) and return the page the user lands on.
Tests never contain CSS selectors.
Check: no `$("...")` in `src/test`.

**5.2 Fluent navigation: an action returns the next page object.** 🟡
`LoginSignupPage.open().loginAs(...)` returns `HomePage`, and `modal.viewCart()` returns `CartPage`.
Use a separate `...ExpectingError` method when the same action stays on the page.

**5.3 Reusable parts of a page are components.** 🟡
`Header`, `AddedToCartModal`, `CartRow`. Expose them through accessors (`page.header()`), not public fields.

**5.4 Stable selectors, in this order: `data-qa` / `data-test` > `id` > a short CSS path tied to meaning.** 🟠
Never use XPath based on position or layout classes, such as `div[3]/span` or `.col-sm-4`.

**5.5 Open pages with absolute URLs from `Config`.** 🔴
`Selenide.open(Config.aeUrl() + "/login")`. A global `Configuration.baseUrl` races when classes for different sites run in parallel.

**5.6 Actions that trigger AJAX wait for their effect.** 🟠
Example: `CartRow.remove()` clicks, then waits for the row to disappear. The next step then never acts on a page that is still changing.

**5.7 Mask secrets typed into the UI.** 🔴
`setValue(withText(password).sensitive())`. Otherwise passwords appear in logs and in the public Allure report.

**5.8 One fresh browser per test, closed in `@AfterEach`.** 🟠
`BaseUiTest` handles it. No leftover cookies, cart or login between tests.

## 6. Parallel execution and thread safety

Test classes run concurrently (`src/test/resources/junit-platform.properties`), and methods inside a class run in order.

**6.1 No mutable static state shared between test classes.** 🔴
Global Selenide `Configuration` is set once, in `BaseUiTest`'s static initialiser, with the same values for everyone.
Never change `Configuration.*` from a test.

**6.2 Per-thread or per-test for anything that is not thread-safe.** 🔴
The WebDriver is per thread (Selenide), `Faker` is per thread (`ThreadLocal`), fixtures go in the JUnit store of each test.
OkHttp clients, Retrofit services and the `ObjectMapper` are thread-safe and shared.

**6.3 Selenide listeners are per thread: add them in `@BeforeEach`.** 🟠
The Allure listener is added in `BaseUiTest`, not in a static block.

**6.4 A test that can't run in parallel is marked, not hidden.** 🟠
Use `@Isolated` or `@ResourceLock("...")` with a comment explaining why. Never turn parallelism off globally to hide a race.

## 7. Flakiness

**7.1 No `Thread.sleep`, no fixed waits.** 🔴
Selenide `should...` conditions wait smartly up to `Configuration.timeout`. A sleep is either too long (slow) or too short (flaky).

**7.2 No automatic retries of failed tests.** 🟠
Retries hide real bugs and races. Find the cause and fix it at the source, then document it in a comment.
Examples from this repo:
- Full-screen Google ads hijacked clicks. Ad domains are blocked with Chrome `--host-resolver-rules`.
- Restful-booker resets its data every few minutes. Each test gets a fresh token.
- Reqres limited anonymous use to 40 requests a day. Removed, because a suite must not depend on a quota.

**7.3 Prove a fix with repeated runs.** 🟡
Before calling a flaky test fixed, run it several times in a loop, alone and in the full parallel suite.

**7.4 Test results are never cached.** 🔴
These tests hit live systems: `outputs.upToDateWhen { false }` and `outputs.cacheIf { false }` in `build.gradle`.
A cached green result would be a false pass.

## 8. Reporting (Allure)

**8.1 Every test class has `@Epic` (application) and `@Feature` (area); critical paths have `@Severity`.** 🟡
The report is grouped by these labels.

**8.2 Page-object actions and fixtures are `@Step`s with readable names and parameters.** 🟡
`@Step("Log in as {email}")`, which needs the `-parameters` compiler flag (already on).
Never put secrets in step names.

**8.3 Failures carry evidence.** 🟠
UI tests get a screenshot and page source automatically, and API calls get request/response attachments.
Error messages say what was expected, what came back, and for which request.

## 9. Configuration and secrets

**9.1 All URLs and credentials come from `Config`, with `-D` overrides.** 🟠
No URLs or credentials scattered through tests. A new value gets a method in `Config` with a sensible default.

**9.2 Never commit real secrets.** 🔴
The defaults in this repo are public demo credentials only. Real credentials come from CI secrets
(`${{ secrets.X }}` passed as `-D`), never from files in git.

**9.3 Logs and reports must not leak tokens or passwords.** 🔴
Failure messages print method and URL only, the Selenide password fields are `sensitive()`, and the console HTTP log is at BASIC level.

## 10. Build, dependencies and CI

**10.1 Versions live only in `gradle/libs.versions.toml`.** 🟡
Use BOMs (JUnit, Allure) and no versions in `build.gradle`. Dependabot updates them weekly.

**10.2 Framework code goes in `src/main` and tests in `src/test`; dependency scopes match.** 🟡
Test-only libraries (JUnit, AssertJ) are `testImplementation`. If `src/main` needs it, it is `implementation`.

**10.3 CI is the source of truth.** 🟠
"Works on my machine" is not done. Suites run as parallel jobs on push, pull request and nightly,
with a timeout per job, least-privilege `permissions`, and the Allure report published to GitHub Pages from `main`.

**10.4 A red nightly run gets investigated the same day.** 🟠
It means either the application changed or a test is flaky. Both are worth knowing.

## 11. Code style

**11.1 Match the surrounding code**: naming, structure, comment density. 🟡
Comments explain **why** (a site quirk, a workaround), not what the code obviously does.

**11.2 No dead code or unused imports and methods.** 🟡
If nothing calls it, delete it; git remembers.

**11.3 Small, focused commits with messages that explain the change.** 🟡

---

## Review checklist

Copy into a PR review and tick what applies. The numbers point to the rules above.

**🔴 Blockers**
- [ ] Tests are independent and use unique, self-cleaning data (1.2, 2.1)
- [ ] Assertions check the real status and body; no false passes (3.3, 7.4)
- [ ] No shared mutable static state; nothing that races in parallel (5.5, 6.1, 6.2)
- [ ] No `Thread.sleep` / fixed waits (7.1)
- [ ] No secrets in code, logs, step names or reports (5.7, 9.2, 9.3)

**🟠 Major**
- [ ] Clear `@DisplayName`, one behaviour per test, `@Tag` present (1.1, 1.6)
- [ ] Preconditions through the API or fixtures, not UI clicks (1.3, 2.3)
- [ ] Cleanup in an extension or `@AfterEach` (2.2)
- [ ] No assertions in `src/main`, no selectors in `src/test` (3.1, 5.1)
- [ ] UI checks use Selenide conditions, not `getText()` + `assertThat` (3.2)
- [ ] New endpoints are in the Retrofit interface with typed models (4.1, 4.2)
- [ ] Stable selectors; AJAX actions wait for their effect (5.4, 5.6)
- [ ] Flakiness fixed at the source, no retries (7.2)

**🟡 Minor**
- [ ] Parameterized instead of copy-paste; negative cases covered (1.4, 1.5)
- [ ] Allure labels and steps present (8.1, 8.2)
- [ ] Versions in the catalog; correct dependency scopes (10.1, 10.2)
- [ ] No dead code; comments explain why (11.1, 11.2)
