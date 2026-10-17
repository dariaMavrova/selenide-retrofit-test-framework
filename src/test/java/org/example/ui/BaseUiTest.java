package org.example.ui;

import com.codeborne.selenide.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;

import static com.codeborne.selenide.Selenide.closeWebDriver;

/**
 * Browser setup shared by all UI tests. Each site's tests say which base URL they run against.
 * A fresh browser per test keeps tests independent (no leftover cookies, cart or session).
 */
@Tag("ui")
public abstract class BaseUiTest {

    protected abstract String baseUrl();

    @BeforeEach
    void configureBrowser() {
        Configuration.baseUrl = baseUrl();
        Configuration.browser = System.getProperty("selenide.browser", "chrome");
        Configuration.headless = Boolean.parseBoolean(System.getProperty("selenide.headless", "true"));
        Configuration.browserSize = "1920x1080";
        Configuration.timeout = 10_000;
        Configuration.pageLoadStrategy = "eager";
        Configuration.reportsFolder = "build/reports/selenide";
        configureBrowserOptions();
    }

    /** Hook for site-specific browser options. */
    protected void configureBrowserOptions() {
    }

    @AfterEach
    void closeBrowser() {
        closeWebDriver();
    }
}
