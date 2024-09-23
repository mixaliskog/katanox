package com.katanox.api.util;

import java.time.LocalDate;

import com.katanox.api.domain.booking.Guest;
import com.katanox.api.domain.booking.Payment;

public class TestUtil {
    public static final LocalDate firstOfMarch = LocalDate.of(2022, 3, 1);
    public static final LocalDate secondOfMarch = LocalDate.of(2022, 3, 2);
    public static final LocalDate sixthOfMarch = LocalDate.of(2022, 3, 6);
    public static final LocalDate seventhOfMarch = LocalDate.of(2022, 3, 7);
    public static final Long hotelId1 = 1L;
    public static final Long bookingId = 1L;
    public static final Long roomId1 = 1L;
    public static final Long hotelId2 = 2L;
    public static final String currency = "EUR";
    public static final Guest guest = new Guest("Name", "surname", LocalDate.of(1995, 1, 1));
    public static final Payment payment = new Payment("Name", "123456", "213", "03", "2034");

}
