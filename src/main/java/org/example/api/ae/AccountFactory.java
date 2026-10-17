package org.example.api.ae;

import net.datafaker.Faker;
import org.example.api.ae.model.Account;

import java.util.UUID;

public final class AccountFactory {

    // One Faker per thread: tests run in parallel and Faker is not documented as thread-safe.
    private static final ThreadLocal<Faker> FAKER = ThreadLocal.withInitial(Faker::new);
    private static final String[] COUNTRIES =
            {"India", "United States", "Canada", "Australia", "Israel", "New Zealand", "Singapore"};

    private AccountFactory() {
    }

    /** A random account with a unique e-mail, so parallel runs never collide. */
    public static Account randomAccount() {
        Faker faker = FAKER.get();
        String firstName = faker.name().firstName();
        String lastName = faker.name().lastName();
        String uniqueEmail = "aqa." + UUID.randomUUID().toString().substring(0, 12) + "@example.com";
        return new Account(
                firstName + " " + lastName,
                uniqueEmail,
                faker.credentials().password(10, 16, true, false, true),
                faker.options().option("Mr", "Mrs"),
                faker.number().numberBetween(1, 28),
                faker.number().numberBetween(1, 12),
                faker.number().numberBetween(1960, 2005),
                firstName,
                lastName,
                faker.company().name(),
                faker.address().streetAddress(),
                faker.address().secondaryAddress(),
                faker.options().option(COUNTRIES),
                faker.address().state(),
                faker.address().city(),
                faker.address().zipCode(),
                faker.phoneNumber().cellPhone());
    }
}
