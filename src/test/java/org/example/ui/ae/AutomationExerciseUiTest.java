package org.example.ui.ae;

import com.codeborne.selenide.Configuration;
import org.example.config.Config;
import org.example.ui.BaseUiTest;
import org.openqa.selenium.chrome.ChromeOptions;

/**
 * automationexercise.com serves Google ads, including full-screen "vignette" ads that hijack clicks.
 * Resolving the ad domains to nothing makes the UI tests fast and stable.
 */
abstract class AutomationExerciseUiTest extends BaseUiTest {

    private static final String BLOCKED_HOSTS = String.join(", ",
            "MAP *.googlesyndication.com ~NOTFOUND",
            "MAP *.doubleclick.net ~NOTFOUND",
            "MAP *.googleadservices.com ~NOTFOUND",
            "MAP *.adtrafficquality.google ~NOTFOUND",
            "MAP fundingchoicesmessages.google.com ~NOTFOUND",
            "MAP *.google-analytics.com ~NOTFOUND",
            "MAP *.googletagmanager.com ~NOTFOUND");

    @Override
    protected String baseUrl() {
        return Config.aeUrl();
    }

    @Override
    protected void configureBrowserOptions() {
        if ("chrome".equals(Configuration.browser)) {
            Configuration.browserCapabilities = new ChromeOptions()
                    .addArguments("--host-resolver-rules=" + BLOCKED_HOSTS);
        }
    }
}
