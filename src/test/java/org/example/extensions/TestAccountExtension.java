package org.example.extensions;

import io.qameta.allure.Allure;
import org.example.api.ApiClients;
import org.example.api.ae.AutomationExerciseApi;
import org.example.api.ae.model.Account;
import org.example.api.ae.model.MessageResponse;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.util.ArrayList;
import java.util.List;

import static org.example.api.ApiCalls.execute;
import static org.example.api.ApiCalls.executeAndExpect;
import static org.example.api.ae.AccountFactory.randomAccount;

/**
 * Resolves {@link TestAccount} parameters: creates test data through the API (fast, no UI)
 * and cleans it up afterwards. Accounts are kept in the JUnit store of the current test, so parallel tests
 * never share or delete each other's data.
 */
public class TestAccountExtension implements ParameterResolver, AfterEachCallback {

    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(TestAccountExtension.class);
    private static final String ACCOUNTS = "accounts";

    private final AutomationExerciseApi api = ApiClients.automationExercise();

    @Override
    public boolean supportsParameter(ParameterContext parameter, ExtensionContext context) {
        return parameter.getParameter().getType() == Account.class && parameter.isAnnotated(TestAccount.class);
    }

    @Override
    public Account resolveParameter(ParameterContext parameter, ExtensionContext context) {
        Account account = randomAccount();
        accounts(context).add(account);

        boolean registered = parameter.findAnnotation(TestAccount.class).orElseThrow().registered();
        if (registered) {
            Allure.step("Precondition: register " + account.email() + " via API", () -> {
                MessageResponse response = executeAndExpect(api.createAccount(account.toFormFields()), 200);
                if (response.responseCode() != 201) {
                    throw new IllegalStateException("Could not create test account: " + response);
                }
            });
        }
        return account;
    }

    @Override
    public void afterEach(ExtensionContext context) {
        for (Account account : accounts(context)) {
            Allure.step("Cleanup: delete " + account.email() + " via API",
                    () -> execute(api.deleteAccount(account.email(), account.password())));
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Account> accounts(ExtensionContext context) {
        return context.getStore(NAMESPACE).computeIfAbsent(ACCOUNTS, key -> new ArrayList<Account>(), List.class);
    }
}
