package com.katanox.api.domain.booking;

import java.time.LocalDate;

import com.katanox.test.sql.enums.BookingStatus;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@EqualsAndHashCode
@NoArgsConstructor
public class BookingRequest {
  public Long hotelId;
  public Guest guest;
  public Long roomId;
  public LocalDate checkin;
  public LocalDate checkout;
  public String currency;
  public Payment payment;

  public BookingDTO toBookingDTO(final Long bookingId, final AvailableRoom availableRoom) {
    return new BookingDTO(hotelId, bookingId, guest, availableRoom, checkin, checkout, currency, payment, BookingStatus.pending);
  }

}
