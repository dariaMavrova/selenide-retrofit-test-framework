package org.example.api.booker.model;

import java.time.LocalDate;

public record BookingDates(LocalDate checkin, LocalDate checkout) {
}
