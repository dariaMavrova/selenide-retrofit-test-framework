package org.example.api.ae;

import net.datafaker.Faker;
import org.example.api.ae.model.Account;

import java.util.UUID;

public final class AccountFactory {

    private static final Faker FAKER = new Faker();
    private static final String[] COUNTRIES =
            {"India", "United States", "Canada", "Australia", "Israel", "New Zealand", "Singapore"};

    private AccountFactory() {
    }

    /** A random account with a unique e-mail, so parallel runs never collide. */
    public static Account randomAccount() {
        String firstName = FAKER.name().firstName();
        String lastName = FAKER.name().lastName();
        String uniqueEmail = "aqa." + UUID.randomUUID().toString().substring(0, 12) + "@example.com";
        return new Account(
                firstName + " " + lastName,
                uniqueEmail,
                FAKER.internet().password(10, 16, true, false, true),
                FAKER.options().option("Mr", "Mrs"),
                FAKER.number().numberBetween(1, 28),
                FAKER.number().numberBetween(1, 12),
                FAKER.number().numberBetween(1960, 2005),
                firstName,
                lastName,
                FAKER.company().name(),
                FAKER.address().streetAddress(),
                FAKER.address().secondaryAddress(),
                FAKER.options().option(COUNTRIES),
                FAKER.address().state(),
                FAKER.address().city(),
                FAKER.address().zipCode(),
                FAKER.phoneNumber().cellPhone());
    }
}
