package org.example.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.time.Duration;

/**
 * Builds Retrofit services. All services share one connection pool and logging setup;
 * each one can add its own interceptors (auth headers etc.).
 */
public final class RetrofitClient {

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
        // Logging goes last so it shows the headers added by the interceptors above.
        client.addInterceptor(loggingInterceptor());

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

    private static HttpLoggingInterceptor loggingInterceptor() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        logging.redactHeader("x-api-key");
        logging.redactHeader("Cookie");
        return logging;
    }
}
