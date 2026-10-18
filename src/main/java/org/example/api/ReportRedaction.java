package org.example.api;

import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.util.Set;

/**
 * Keeps secrets in form bodies out of the (public) Allure report.
 * <p>
 * Allure 3.0's OkHttp integration records form bodies verbatim: {@code redactFormParameter} only applies to parsed
 * forms, which it never produces for OkHttp. So the interceptor before Allure's swaps in a copy with secrets masked,
 * and the one after it puts the original body back before the request is sent.
 * Order in the chain: {@link #maskFormSecrets()}, {@code AllureOkHttp3}, {@link #restoreFormSecrets()}.
 */
final class ReportRedaction {

    static final String REDACTED = "__ALLURE_REDACTED__";
    private static final Set<String> SECRET_FIELDS = Set.of("password");

    /** Carries the real body past the Allure interceptor. */
    private record OriginalBody(RequestBody body) {
    }

    private ReportRedaction() {
    }

    static Interceptor maskFormSecrets() {
        return chain -> {
            Request request = chain.request();
            if (!(request.body() instanceof FormBody form) || !hasSecret(form)) {
                return chain.proceed(request);
            }
            FormBody.Builder masked = new FormBody.Builder();
            for (int i = 0; i < form.size(); i++) {
                masked.addEncoded(form.encodedName(i), SECRET_FIELDS.contains(form.name(i)) ? REDACTED : form.encodedValue(i));
            }
            return chain.proceed(request.newBuilder()
                    .method(request.method(), masked.build())
                    .tag(OriginalBody.class, new OriginalBody(form))
                    .build());
        };
    }

    static Interceptor restoreFormSecrets() {
        return chain -> {
            Request request = chain.request();
            OriginalBody original = request.tag(OriginalBody.class);
            return chain.proceed(original == null
                    ? request
                    : request.newBuilder().method(request.method(), original.body()).build());
        };
    }

    private static boolean hasSecret(FormBody form) {
        for (int i = 0; i < form.size(); i++) {
            if (SECRET_FIELDS.contains(form.name(i))) {
                return true;
            }
        }
        return false;
    }
}
