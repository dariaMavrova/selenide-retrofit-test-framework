package org.example.api.ae.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A user account, used both for API calls (as form fields) and for filling the UI sign-up form.
 */
public record Account(
        String name,
        String email,
        String password,
        String title,
        int birthDay,
        int birthMonth,
        int birthYear,
        String firstName,
        String lastName,
        String company,
        String address1,
        String address2,
        String country,
        String state,
        String city,
        String zipcode,
        String mobileNumber) {

    public Account withName(String name) {
        return new Account(name, email, password, title, birthDay, birthMonth, birthYear, firstName, lastName,
                company, address1, address2, country, state, city, zipcode, mobileNumber);
    }

    public Account withCity(String city) {
        return new Account(name, email, password, title, birthDay, birthMonth, birthYear, firstName, lastName,
                company, address1, address2, country, state, city, zipcode, mobileNumber);
    }

    /** Field names expected by POST /createAccount and PUT /updateAccount. */
    public Map<String, String> toFormFields() {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("name", name);
        fields.put("email", email);
        fields.put("password", password);
        fields.put("title", title);
        fields.put("birth_date", String.valueOf(birthDay));
        fields.put("birth_month", String.valueOf(birthMonth));
        fields.put("birth_year", String.valueOf(birthYear));
        fields.put("firstname", firstName);
        fields.put("lastname", lastName);
        fields.put("company", company);
        fields.put("address1", address1);
        fields.put("address2", address2);
        fields.put("country", country);
        fields.put("zipcode", zipcode);
        fields.put("state", state);
        fields.put("city", city);
        fields.put("mobile_number", mobileNumber);
        return fields;
    }
}
