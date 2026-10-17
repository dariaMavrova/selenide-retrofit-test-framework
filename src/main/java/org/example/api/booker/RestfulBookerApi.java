package org.example.api.booker;

import org.example.api.booker.model.AuthRequest;
import org.example.api.booker.model.AuthResponse;
import org.example.api.booker.model.Booking;
import org.example.api.booker.model.BookingId;
import org.example.api.booker.model.CreatedBooking;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.util.List;

/**
 * Retrofit contract for https://restful-booker.herokuapp.com/apidoc
 * Write operations need an auth token, sent as {@code Cookie: token=<token>} (see {@link #tokenCookie}).
 */
public interface RestfulBookerApi {

    @GET("ping")
    Call<Void> ping();

    @POST("auth")
    Call<AuthResponse> createToken(@Body AuthRequest request);

    /** Null filters are left out of the query string. */
    @GET("booking")
    Call<List<BookingId>> getBookingIds(@Query("firstname") String firstname, @Query("lastname") String lastname);

    @GET("booking/{id}")
    Call<Booking> getBooking(@Path("id") int id);

    @POST("booking")
    Call<CreatedBooking> createBooking(@Body Booking booking);

    @PUT("booking/{id}")
    Call<Booking> updateBooking(@Path("id") int id, @Body Booking booking, @Header("Cookie") String cookie);

    /** Only non-null fields of {@code patch} are sent. */
    @PATCH("booking/{id}")
    Call<Booking> partialUpdateBooking(@Path("id") int id, @Body Booking patch, @Header("Cookie") String cookie);

    @DELETE("booking/{id}")
    Call<Void> deleteBooking(@Path("id") int id, @Header("Cookie") String cookie);

    static String tokenCookie(String token) {
        return "token=" + token;
    }
}
