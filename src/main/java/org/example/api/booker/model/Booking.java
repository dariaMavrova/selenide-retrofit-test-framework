package org.example.api.booker.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Booking(
        String firstname,
        String lastname,
        Integer totalprice,
        Boolean depositpaid,
        BookingDates bookingdates,
        String additionalneeds) {

    public Booking withFirstname(String firstname) {
        return new Booking(firstname, lastname, totalprice, depositpaid, bookingdates, additionalneeds);
    }

    public Booking withTotalprice(Integer totalprice) {
        return new Booking(firstname, lastname, totalprice, depositpaid, bookingdates, additionalneeds);
    }
}
