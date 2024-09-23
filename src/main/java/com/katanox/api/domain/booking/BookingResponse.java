package com.katanox.api.domain.booking;

import java.time.LocalDate;

import com.katanox.test.sql.enums.BookingStatus;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class BookingResponse {
        public Long hotelId;
        public Long bookingId;
        public Guest guest;
        public AvailableRoom room;
        public LocalDate checkin;
        public LocalDate checkout;
        public BookingStatus status;

        public static BookingResponse from(final BookingDTO booking) {
                return new BookingResponse(booking.hotelId,
                    booking.bookingId,
                    booking.guest,
                    booking.room,
                    booking.checkin,
                    booking.checkout,
                    booking.status);
        }
}




