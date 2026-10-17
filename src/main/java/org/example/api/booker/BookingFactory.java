package org.example.api.booker;

import net.datafaker.Faker;
import org.example.api.booker.model.Booking;
import org.example.api.booker.model.BookingDates;

import java.time.LocalDate;

/**
 * Random but valid bookings, so tests never depend on data created by someone else.
 */
public final class BookingFactory {

    private static final Faker FAKER = new Faker();

    private BookingFactory() {
    }

    public static Booking randomBooking() {
        LocalDate checkin = LocalDate.now().plusDays(FAKER.number().numberBetween(1, 60));
        return new Booking(
                FAKER.name().firstName(),
                FAKER.name().lastName(),
                FAKER.number().numberBetween(50, 2000),
                FAKER.bool().bool(),
                new BookingDates(checkin, checkin.plusDays(FAKER.number().numberBetween(1, 14))),
                FAKER.options().option("Breakfast", "Late checkout", "Airport transfer", "Parking"));
    }
}
