package org.example.ui.pages.ae;

import io.qameta.allure.Step;
import com.codeborne.selenide.SelenideElement;
import org.example.api.ae.model.Account;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.SetValueOptions.withText;
import static com.codeborne.selenide.Selenide.page;

/**
 * /signup: "Enter Account Information" form, reached after the first sign-up step.
 */
public class SignupPage {

    private final SelenideElement name = $("[data-qa='name']");
    private final SelenideElement email = $("[data-qa='email']");

    public SelenideElement name() {
        return name;
    }

    public SelenideElement email() {
        return email;
    }

    @Step("Fill in account information and create the account")
    public AccountCreatedPage fillAndSubmit(Account account) {
        $(account.title().equals("Mr") ? "#id_gender1" : "#id_gender2").click();
        $("[data-qa='password']").setValue(withText(account.password()).sensitive());
        $("[data-qa='days']").selectOptionByValue(String.valueOf(account.birthDay()));
        $("[data-qa='months']").selectOptionByValue(String.valueOf(account.birthMonth()));
        $("[data-qa='years']").selectOptionByValue(String.valueOf(account.birthYear()));
        $("[data-qa='first_name']").setValue(account.firstName());
        $("[data-qa='last_name']").setValue(account.lastName());
        $("[data-qa='company']").setValue(account.company());
        $("[data-qa='address']").setValue(account.address1());
        $("[data-qa='address2']").setValue(account.address2());
        $("[data-qa='country']").selectOption(account.country());
        $("[data-qa='state']").setValue(account.state());
        $("[data-qa='city']").setValue(account.city());
        $("[data-qa='zipcode']").setValue(account.zipcode());
        $("[data-qa='mobile_number']").setValue(account.mobileNumber());
        $("[data-qa='create-account']").click();
        return page(AccountCreatedPage.class);
    }
}
