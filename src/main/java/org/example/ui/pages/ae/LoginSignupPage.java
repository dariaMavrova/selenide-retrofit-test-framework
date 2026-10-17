package org.example.ui.pages.ae;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.page;

/**
 * /login: the page has both the "Login" and the "New User Signup!" forms.
 */
public class LoginSignupPage {

    public final Header header = new Header();

    private final SelenideElement loginEmail = $("[data-qa='login-email']");
    private final SelenideElement loginPassword = $("[data-qa='login-password']");
    private final SelenideElement loginButton = $("[data-qa='login-button']");
    private final SelenideElement loginError = $("form[action='/login'] p");

    private final SelenideElement signupName = $("[data-qa='signup-name']");
    private final SelenideElement signupEmail = $("[data-qa='signup-email']");
    private final SelenideElement signupButton = $("[data-qa='signup-button']");
    private final SelenideElement signupError = $("form[action='/signup'] p");

    public static LoginSignupPage open() {
        Selenide.open("/login");
        return page(LoginSignupPage.class);
    }

    public HomePage loginAs(String email, String password) {
        fillLogin(email, password);
        return page(HomePage.class);
    }

    public LoginSignupPage loginExpectingError(String email, String password) {
        fillLogin(email, password);
        return this;
    }

    public SignupPage startSignup(String name, String email) {
        fillSignup(name, email);
        return page(SignupPage.class);
    }

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
        loginPassword.setValue(password);
        loginButton.click();
    }

    private void fillSignup(String name, String email) {
        signupName.setValue(name);
        signupEmail.setValue(email);
        signupButton.click();
    }
}
