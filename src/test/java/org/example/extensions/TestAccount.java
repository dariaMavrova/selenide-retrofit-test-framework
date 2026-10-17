package org.example.extensions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Injects a random AutomationExercise {@link org.example.api.ae.model.Account} into a test or lifecycle method.
 * The account is deleted through the API after the test, whether it passed or not.
 *
 * <pre>{@code
 * @Test
 * void login(@TestAccount Account account) { ... }                    // already registered via API
 *
 * @Test
 * void register(@TestAccount(registered = false) Account account) { ... }  // only data; test registers it
 * }</pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface TestAccount {

    /** Whether to create the account through the API before the test starts. */
    boolean registered() default true;
}
