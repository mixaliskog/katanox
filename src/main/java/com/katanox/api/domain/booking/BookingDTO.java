package com.katanox.api.domain.booking;

import java.time.LocalDate;

import com.katanox.test.sql.enums.BookingStatus;
import com.katanox.test.sql.tables.records.BookingPaymentsRecord;
import com.katanox.test.sql.tables.records.BookingRoomsRecord;
import com.katanox.test.sql.tables.records.BookingsRecord;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;

@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class BookingDTO {
    public Long hotelId;
    public Long bookingId;
    public Guest guest;
    public AvailableRoom room;
    public LocalDate checkin;
    public LocalDate checkout;
    public String currency;
    public Payment payment;
    public BookingStatus status;

    public static BookingDTO fromBookingRecord(final BookingsRecord record, final BookingRoomsRecord bookingRoomsRecord, final BookingPaymentsRecord bookingPaymentsRecord) {
        return BookingDTO.builder().bookingId(record.getId())
            .checkin(record.getCheckinDate())
            .checkout(record.getCheckoutDate())
            .status(record.getStatus())
            .guest(new Guest(record.getGuestName(), record.getGuestSurname(), record.getGuestDateOfBirth()))
            .hotelId(record.getHotelId())
            .room(AvailableRoom.fromDBRecord(bookingRoomsRecord))
            .payment(Payment.fromDBRecord(bookingPaymentsRecord))
            .currency(record.getCurrency())
            .build();
    }
}
