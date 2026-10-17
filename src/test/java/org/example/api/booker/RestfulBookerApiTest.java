package org.example.api.booker;

import org.example.api.ApiClients;
import org.example.api.booker.model.AuthRequest;
import org.example.api.booker.model.AuthResponse;
import org.example.api.booker.model.Booking;
import org.example.api.booker.model.BookingId;
import org.example.api.booker.model.CreatedBooking;
import org.example.config.Config;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.api.ApiCalls.execute;
import static org.example.api.ApiCalls.executeAndExpect;
import static org.example.api.booker.BookingFactory.randomBooking;
import static org.example.api.booker.RestfulBookerApi.tokenCookie;

@Tag("api")
@DisplayName("Restful-booker API")
class RestfulBookerApiTest {

    private static final RestfulBookerApi api = ApiClients.restfulBooker();
    private static String token;

    @BeforeAll
    static void authenticate() {
        executeAndExpect(api.ping(), 201);
        token = executeAndExpect(api.createToken(
                new AuthRequest(Config.bookerUsername(), Config.bookerPassword())), 200).token();
    }

    @Nested
    @DisplayName("POST /auth")
    class Auth {

        @Test
        @DisplayName("valid credentials return a token")
        void validCredentials() {
            assertThat(token).isNotBlank();
        }

        @Test
        @DisplayName("bad credentials return a reason and no token")
        void badCredentials() {
            AuthResponse response = executeAndExpect(api.createToken(new AuthRequest("admin", "wrong")), 200);

            assertThat(response.token()).isNull();
            assertThat(response.reason()).isEqualTo("Bad credentials");
        }
    }

    @Nested
    @DisplayName("Booking CRUD")
    class Crud {

        private Booking booking;
        private int bookingId;

        @BeforeEach
        void createBooking() {
            booking = randomBooking();
            CreatedBooking created = executeAndExpect(api.createBooking(booking), 200);
            bookingId = created.bookingid();

            assertThat(created.booking()).isEqualTo(booking);
        }

        @AfterEach
        void deleteBooking() {
            // Best-effort cleanup: the delete test removes the booking itself.
            execute(api.deleteBooking(bookingId, tokenCookie(token)));
        }

        @Test
        @DisplayName("GET returns the booking exactly as it was created")
        void getCreatedBooking() {
            assertThat(executeAndExpect(api.getBooking(bookingId), 200)).isEqualTo(booking);
        }

        @Test
        @DisplayName("GET /booking filters by name")
        void filterByName() {
            List<BookingId> ids = executeAndExpect(
                    api.getBookingIds(booking.firstname(), booking.lastname()), 200);

            assertThat(ids).extracting(BookingId::bookingid).contains(bookingId);
        }

        @Test
        @DisplayName("PUT replaces the booking")
        void fullUpdate() {
            Booking changed = randomBooking();

            Booking response = executeAndExpect(api.updateBooking(bookingId, changed, tokenCookie(token)), 200);

            assertThat(response).isEqualTo(changed);
            assertThat(executeAndExpect(api.getBooking(bookingId), 200)).isEqualTo(changed);
        }

        @Test
        @DisplayName("PATCH changes only the sent fields")
        void partialUpdate() {
            Booking patch = new Booking("Patched", null, 999, null, null, null);

            Booking response = executeAndExpect(
                    api.partialUpdateBooking(bookingId, patch, tokenCookie(token)), 200);

            assertThat(response).isEqualTo(booking.withFirstname("Patched").withTotalprice(999));
        }

        @Test
        @DisplayName("PUT without a token is forbidden and changes nothing")
        void updateWithoutToken() {
            assertThat(execute(api.updateBooking(bookingId, randomBooking(), null)).code()).isEqualTo(403);
            assertThat(executeAndExpect(api.getBooking(bookingId), 200)).isEqualTo(booking);
        }

        @Test
        @DisplayName("DELETE removes the booking")
        void deleteRemovesBooking() {
            executeAndExpect(api.deleteBooking(bookingId, tokenCookie(token)), 201);

            assertThat(execute(api.getBooking(bookingId)).code()).isEqualTo(404);
        }
    }
}
