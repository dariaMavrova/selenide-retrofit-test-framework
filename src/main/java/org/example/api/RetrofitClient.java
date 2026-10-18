package org.example.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.qameta.allure.okhttp3.AllureOkHttp3;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.time.Duration;
import java.util.List;

/**
 * Builds Retrofit services. All services share one connection pool, JSON mapper and logging setup;
 * each one can add its own interceptors (auth headers etc.). OkHttp clients and the mapper are thread-safe.
 */
public final class RetrofitClient {

    private static final Logger LOG = LoggerFactory.getLogger("org.example.api.http");

    public static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private static final OkHttpClient BASE_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(10))
            .readTimeout(Duration.ofSeconds(30))
            .build();

    private RetrofitClient() {
    }

    public static <T> T create(String baseUrl, Class<T> service, Interceptor... interceptors) {
        OkHttpClient.Builder client = BASE_CLIENT.newBuilder();
        for (Interceptor interceptor : interceptors) {
            client.addInterceptor(interceptor);
        }
        // Added last so they see the final request, including headers from the interceptors above.
        client.addInterceptor(ReportRedaction.maskFormSecrets());
        client.addInterceptor(allureAttachments());
        client.addInterceptor(ReportRedaction.restoreFormSecrets());
        client.addInterceptor(consoleLogging());

        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client.build())
                .addConverterFactory(JacksonConverterFactory.create(MAPPER))
                .build()
                .create(service);
    }

    public static Interceptor header(String name, String value) {
        return chain -> chain.proceed(chain.request().newBuilder().header(name, value).build());
    }

    /**
     * Records each request/response in the Allure report. The report is public (GitHub Pages),
     * so credentials are redacted: auth headers and the Restful-booker token cookie here,
     * passwords in form bodies by {@link ReportRedaction}.
     */
    private static AllureOkHttp3 allureAttachments() {
        return new AllureOkHttp3().configureHttpExchange(exchange -> exchange
                .redactHeaders(List.of("Authorization", "Cookie", "X-Api-Key"))
                .redactCookie("token"));
    }

    /** One line per request in the console; full bodies go to the Allure report. */
    private static HttpLoggingInterceptor consoleLogging() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(LOG::info);
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        return logging;
    }
}
