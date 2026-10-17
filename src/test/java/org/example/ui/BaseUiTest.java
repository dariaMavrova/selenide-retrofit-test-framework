package org.example.ui;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.logevents.SelenideLogger;
import io.qameta.allure.selenide.AllureSelenide;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.openqa.selenium.chrome.ChromeOptions;

import static com.codeborne.selenide.Selenide.closeWebDriver;

/**
 * Browser setup shared by all UI tests.
 * <ul>
 *   <li>Configuration is global in Selenide, so it is set exactly once (class initialisation is thread-safe)
 *       and page objects use absolute URLs instead of a shared {@code Configuration.baseUrl}.</li>
 *   <li>The WebDriver is per thread in Selenide, and every test gets a fresh browser:
 *       no leftover cookies, cart or session, and safe parallel execution.</li>
 * </ul>
 */
@Tag("ui")
public abstract class BaseUiTest {

    /**
     * automationexercise.com serves Google ads, including full-screen "vignette" ads that hijack clicks.
     * Resolving ad domains to nothing makes UI tests fast and stable without sleeps or retries.
     */
    private static final String BLOCKED_HOSTS = String.join(", ",
            "MAP *.googlesyndication.com ~NOTFOUND",
            "MAP *.doubleclick.net ~NOTFOUND",
            "MAP *.googleadservices.com ~NOTFOUND",
            "MAP *.adtrafficquality.google ~NOTFOUND",
            "MAP fundingchoicesmessages.google.com ~NOTFOUND",
            "MAP *.google-analytics.com ~NOTFOUND",
            "MAP *.googletagmanager.com ~NOTFOUND");

    static {
        Configuration.browser = System.getProperty("selenide.browser", "chrome");
        Configuration.headless = Boolean.parseBoolean(System.getProperty("selenide.headless", "true"));
        Configuration.browserSize = "1920x1080";
        Configuration.timeout = 10_000;
        Configuration.pageLoadStrategy = "eager";
        Configuration.reportsFolder = "build/reports/selenide";
        if ("chrome".equals(Configuration.browser)) {
            Configuration.browserCapabilities = new ChromeOptions()
                    .addArguments("--host-resolver-rules=" + BLOCKED_HOSTS);
        }
    }

    @BeforeEach
    void attachAllureListener() {
        // Selenide listeners are per thread: every action becomes an Allure step, failures get a screenshot.
        SelenideLogger.addListener("allure", new AllureSelenide()
                .screenshots(true)
                .savePageSource(true)
                .includeSelenideSteps(true));
    }

    @AfterEach
    void closeBrowser() {
        closeWebDriver();
        SelenideLogger.removeListener("allure");
    }
}
