---
name: aqa-framework
description: Reviews and writes code for this Selenide + Retrofit test automation framework (automationexercise.com UI/API, restful-booker API, herokuapp UI) against the project's best-practices document. Use it whenever the user asks to review code, a diff, a branch or a PR in this repo; to add or change a test, page object, API client, endpoint, model, fixture or a new site/API; to fix a failing or flaky test; or to check "is this good / does this follow best practices", even if they don't name the skill.
---

# AQA framework: review and write

The rules live in **`docs/BEST_PRACTICES.md`**, the single source of truth that both the user and recruiters read.
This skill is the workflow around it, so don't restate rules here or in your output. Read the relevant sections of the document
and cite rules by number (e.g. "5.5"), so the user can look up the reasoning.

Pick the mode from the request:
- **Review**: "review", "check", "is this OK", a PR or branch or diff → [Review](#review)
- **Write**: "add a test for", "cover", "new page / endpoint / site" → [Write](#write)
- **Fix flaky / failing**: "this test fails sometimes", red CI or nightly run → [Fix a flaky or failing test](#fix-a-flaky-or-failing-test)

## Project facts

- Commands: `./gradlew test` (all), `apiTest`, `uiTest`, `test --tests '*CartUiTest'`, `allureReport`.
  Tests are never cached, so every run is a real run. A full run takes about 30 s because classes run in parallel.
- Layout: framework in `src/main/java/org/example/{config,api,ui/pages}`, tests and extensions in `src/test/java/org/example`.
  New site or API = new package `api/<name>` and/or `ui/pages/<name>` plus a test package with the same name.
- Quirks of the systems under test (these explain code that looks odd at first):
  - **AutomationExercise API** always returns HTTP 200; the real status is `responseCode` in the body (`AeAssertions.assertResponse`).
    Form-encoded, and `deleteAccount` is a DELETE with a body (`@HTTP(hasBody = true)`).
  - **AutomationExercise UI** serves full-screen ads, which are blocked in `BaseUiTest` via `--host-resolver-rules`.
    Cart changes happen over AJAX: wait for the effect.
  - **Restful-booker** resets all data every few minutes (fresh token per test), returns 418 without `Accept: application/json`,
    takes auth as `Cookie: token=...`, and answers a failed login with 200 + `reason`.
- Results for inspection: `build/allure-results/*-result.json` (status, message, step tree with attachments) and
  `build/reports/selenide/*.png|html` (screenshot and page source of UI failures).

## Review

1. **Scope.** Find what to review: the files the user named; otherwise `git diff main...HEAD` plus uncommitted changes;
   for a GitHub PR `gh pr diff <n>`. Read each changed file in full, plus the classes it uses, because most
   violations (shared state, missing cleanup) only show up in context.
2. **Mechanical checks.** Run `python3 .claude/skills/aqa-framework/scripts/lint.py` (add file paths to limit it).
   It finds the rules a script can check reliably: sleeps, relative `open()`, `Configuration` writes, selectors in tests,
   assertions in `src/main`, unmasked passwords, missing tags and display names, retries. Treat its findings as candidates and confirm each one.
3. **Judgement checks.** Go through the [Review checklist](../../../docs/BEST_PRACTICES.md#review-checklist) for what
   the script can't see, above all the 🔴 items: Can this test pass while the feature is broken? Does it depend on order or
   shared data? Does anything race when classes run in parallel? Is there a secret in a log, step name or report?
4. **Verify before claiming.** If a finding depends on runtime behaviour (flaky, wrong status, missing cleanup), prove it:
   run the affected tests (`--tests ...`), loop them, or probe the real endpoint with `curl`. Say plainly which findings are
   verified and which are suspected.
5. **Report** in this format, most severe first, and leave out empty sections:

   ```
   ## Review: <scope>

   **Verdict:** <Ready to merge | Fix blockers first | Needs rework>, <one sentence why>

   ### 🔴 Blockers
   - `path/File.java:42`: <what is wrong and the concrete consequence>. **Rule 6.1.** Fix: <specific change>.

   ### 🟠 Major
   ### 🟡 Minor

   ### Good
   - <1–3 things done well, specific; this tells the author what to keep doing>
   ```

   Every finding gets a file and line, the consequence (e.g. "two classes in parallel open the wrong site"), the rule number and a concrete fix.
   Leave out style preferences the document doesn't cover. If something important isn't covered by any rule, report it as
   **"Not in the document yet"** and suggest a rule to add.
6. **Offer to fix.** Ask whether to apply the fixes. If yes, follow [Write](#write) for the changes.

## Write

1. **Learn the pattern first.** Read the closest existing example (e.g. `CartUiTest` + `CartPage` for a UI flow,
   `RestfulBookerApiTest` + `RestfulBookerApi` for CRUD) and [references/recipes.md](references/recipes.md), which has
   templates for each kind of change. New code should look like it was always there.
2. **Look at the real system before writing.** Selectors and behaviour come from the live app, not from memory:
   `curl` the page and grep for `data-qa` / `id`, call the endpoint to see real status codes and body shapes,
   and check what happens on error. Wrong guesses become flaky or false-passing tests.
3. **Write** the framework part (`src/main`: interface method, model, page object, factory) and then the test.
   Preconditions through the API or `@TestAccount`, cleanup in an extension or `@AfterEach`, negative cases next to positive ones.
4. **Prove it.**
   - Run the new tests, then the **full suite** (parallel): a test that passes alone can still race with others.
   - For UI or anything timing-related, run it at least 3 times in a loop.
   - Make sure it can fail. Briefly break the expectation, or reason it through explicitly, so you know the test isn't vacuous.
   - Run `scripts/lint.py` on the changed files.
5. **Update docs.** A new site, API or major feature goes into the README tables and structure. A new convention goes into
   `docs/BEST_PRACTICES.md`, but ask the user first, because the document is shared.

## Fix a flaky or failing test

1. **Reproduce and measure.** Loop the test alone and inside the full suite, e.g.
   `for i in 1 2 3 4 5; do ./gradlew test --tests '*X' 2>&1 | grep -E 'PASSED|FAILED'; done`.
   "Passes alone, fails in the suite" means shared state or load (section 6). "Fails alone too" means timing or the app changed.
2. **Read the evidence.** The failure message and step tree in `build/allure-results/*-result.json`, the screenshot and page
   source in `build/reports/selenide/`, and the site's own JS or API response. Find the actual mechanism. Example from this repo:
   a cart row stayed because the site removes it only after its AJAX call succeeds.
3. **Fix at the source** (7.2): wait for the real effect, remove the shared state, isolate the data, or block the disruptor.
   Never add `Thread.sleep`, retries or a bigger timeout as the fix. Comment the *why* next to the fix.
4. **Prove it** with the same loop as step 1, and say how many runs passed.
5. If the cause is an external service you can't control (quota, outage), tell the user and propose options
   (replace, isolate, or tag it out of CI) instead of masking it.
