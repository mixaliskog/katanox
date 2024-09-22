package com.katanox.api.domain.booking;

import com.katanox.test.sql.enums.BookingStatus;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class UpdateBookingRequest {
    public long bookingId;
    public BookingStatus status;
}
