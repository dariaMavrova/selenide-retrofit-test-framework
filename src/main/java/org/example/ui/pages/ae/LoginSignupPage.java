package org.example.ui.pages.ae;

import org.example.config.Config;
import io.qameta.allure.Step;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.SetValueOptions.withText;
import static com.codeborne.selenide.Selenide.page;

/**
 * /login: the page has both the "Login" and the "New User Signup!" forms.
 */
public class LoginSignupPage {

    private final Header header = new Header();

    public Header header() {
        return header;
    }

    private final SelenideElement loginEmail = $("[data-qa='login-email']");
    private final SelenideElement loginPassword = $("[data-qa='login-password']");
    private final SelenideElement loginButton = $("[data-qa='login-button']");
    private final SelenideElement loginError = $("form[action='/login'] p");

    private final SelenideElement signupName = $("[data-qa='signup-name']");
    private final SelenideElement signupEmail = $("[data-qa='signup-email']");
    private final SelenideElement signupButton = $("[data-qa='signup-button']");
    private final SelenideElement signupError = $("form[action='/signup'] p");

    @Step("Open the Login / Signup page")
    public static LoginSignupPage open() {
        Selenide.open(Config.aeUrl() + "/login");
        return page(LoginSignupPage.class);
    }

    @Step("Log in as {email}")
    public HomePage loginAs(String email, String password) {
        fillLogin(email, password);
        return page(HomePage.class);
    }

    @Step("Try to log in as {email}")
    public LoginSignupPage loginExpectingError(String email, String password) {
        fillLogin(email, password);
        return this;
    }

    @Step("Start sign-up for {name} <{email}>")
    public SignupPage startSignup(String name, String email) {
        fillSignup(name, email);
        return page(SignupPage.class);
    }

    @Step("Try to sign up {name} <{email}>")
    public LoginSignupPage startSignupExpectingError(String name, String email) {
        fillSignup(name, email);
        return this;
    }

    public SelenideElement loginError() {
        return loginError;
    }

    public SelenideElement signupError() {
        return signupError;
    }

    private void fillLogin(String email, String password) {
        loginEmail.setValue(email);
        // sensitive(): the password is masked in logs and in the Allure report.
        loginPassword.setValue(withText(password).sensitive());
        loginButton.click();
    }

    private void fillSignup(String name, String email) {
        signupName.setValue(name);
        signupEmail.setValue(email);
        signupButton.click();
    }
}
