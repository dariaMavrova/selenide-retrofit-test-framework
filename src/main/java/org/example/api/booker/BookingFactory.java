package org.example.api.booker;

import net.datafaker.Faker;
import org.example.api.booker.model.Booking;
import org.example.api.booker.model.BookingDates;

import java.time.LocalDate;

/**
 * Random but valid bookings, so tests never depend on data created by someone else.
 */
public final class BookingFactory {

    // One Faker per thread: tests run in parallel and Faker is not documented as thread-safe.
    private static final ThreadLocal<Faker> FAKER = ThreadLocal.withInitial(Faker::new);

    private BookingFactory() {
    }

    public static Booking randomBooking() {
        Faker faker = FAKER.get();
        LocalDate checkin = LocalDate.now().plusDays(faker.number().numberBetween(1, 60));
        return new Booking(
                faker.name().firstName(),
                faker.name().lastName(),
                faker.number().numberBetween(50, 2000),
                faker.bool().bool(),
                new BookingDates(checkin, checkin.plusDays(faker.number().numberBetween(1, 14))),
                faker.options().option("Breakfast", "Late checkout", "Airport transfer", "Parking"));
    }
}
