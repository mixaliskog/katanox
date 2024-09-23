package com.katanox.api.repository;


import static com.katanox.api.util.TestUtil.currency;
import static com.katanox.api.util.TestUtil.hotelId1;
import static com.katanox.api.util.TestUtil.payment;
import static com.katanox.api.util.TestUtil.roomId1;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.jooq.DSLContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jooq.JooqTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.katanox.api.domain.booking.AvailableRoom;
import com.katanox.api.domain.booking.BookingDTO;
import com.katanox.api.domain.booking.BookingRequest;
import com.katanox.api.domain.booking.Guest;
import com.katanox.api.domain.booking.Payment;
import com.katanox.test.sql.enums.BookingStatus;
import com.katanox.test.sql.tables.BookingPayments;
import com.katanox.test.sql.tables.BookingRooms;
import com.katanox.test.sql.tables.Bookings;

@JooqTest(
    properties = {
        "spring.test.database.replace=none"
    }
)
@RunWith(SpringRunner.class)
public class BookingRepositoryTest {

    @Autowired
    DSLContext dsl;

    private BookingRepository bookingRepository;

    @Before
    public void setUp() {
        this.bookingRepository = new BookingRepository(dsl);
    }

    @Test
    public void testCreateBooking() {
        final Guest guest = new Guest("John", "Doe", LocalDate.of(1980, 5, 10));
        final var bookingRequest = new BookingRequest(hotelId1, guest, roomId1, LocalDate.of(2022, 3, 1), LocalDate.of(2022, 3, 3),
            currency, payment);
        final var availableRoom = AvailableRoom.totalPricesOnly(1L, BigDecimal.valueOf(300), BigDecimal.valueOf(270), currency);

        final var bookingId = bookingRepository.createBooking(bookingRequest, availableRoom);

        assertNotNull(bookingId);

        final var savedBooking = dsl.selectFrom(Bookings.BOOKINGS)
            .where(Bookings.BOOKINGS.ID.eq(bookingId))
            .fetchOne();

        assertNotNull(savedBooking);
        assertEquals(1L, (long) savedBooking.getHotelId());
        assertEquals("John", savedBooking.getGuestName());
        assertEquals("Doe", savedBooking.getGuestSurname());
        assertEquals(LocalDate.of(2022, 3, 1), savedBooking.getCheckinDate());
        assertEquals(LocalDate.of(2022, 3, 3), savedBooking.getCheckoutDate());
        assertEquals(BookingStatus.pending, savedBooking.getStatus());
        assertEquals(currency, savedBooking.getCurrency());

        final var savedBookingRoom = dsl.selectFrom(BookingRooms.BOOKING_ROOMS)
            .where(BookingRooms.BOOKING_ROOMS.BOOKING_ID.eq(bookingId))
            .fetchOne();

        assertEquals(roomId1, savedBookingRoom.getRoomId());
        assertEquals(hotelId1, savedBookingRoom.getHotelId());
        assertEquals(bookingId, savedBookingRoom.getBookingId());
        assertEquals(currency, savedBookingRoom.getCurrency());
        assertEquals(bookingRequest.checkin, savedBookingRoom.getCheckinDate());
        assertEquals(bookingRequest.checkout, savedBookingRoom.getCheckoutDate());

        final var savedBookingPayment = dsl.selectFrom(BookingPayments.BOOKING_PAYMENTS)
            .where(BookingPayments.BOOKING_PAYMENTS.BOOKING_ID.eq(bookingId))
            .fetchOne();

        assertEquals(payment.card_holder, savedBookingPayment.getCardHolder());
        assertEquals(payment.card_number, savedBookingPayment.getCardNumber());
        assertEquals(payment.cvv, savedBookingPayment.getCvv());
        assertEquals(payment.expiry_month, savedBookingPayment.getExpiryMonth());
        assertEquals(payment.expiry_year, savedBookingPayment.getExpiryYear());
    }

    @Test
    public void testUpdateBookingStatus() {
        final Guest guest = new Guest("John", "Doe", LocalDate.of(1980, 5, 10));
        final var bookingRequest = new BookingRequest(hotelId1, guest, roomId1, LocalDate.of(2022, 3, 1), LocalDate.of(2022, 3, 3),
            currency, payment);
        final var availableRoom = AvailableRoom.totalPricesOnly(1L, BigDecimal.valueOf(300), BigDecimal.valueOf(270), "USD");
        final var bookingId = bookingRepository.createBooking(bookingRequest, availableRoom);

        boolean isUpdated = bookingRepository.updateBookingStatus(bookingId, BookingStatus.booked);
        assertTrue(isUpdated);

        // Fetch the booking and check if the status is updated
        final var updatedBooking = dsl.selectFrom(Bookings.BOOKINGS)
            .where(Bookings.BOOKINGS.ID.eq(bookingId))
            .fetchOne();

        assertNotNull(updatedBooking);
        assertEquals(BookingStatus.booked, updatedBooking.getStatus());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateBookingStatus_BookingNotFound() {
        final Long nonExistingBookingId = 999L;
        bookingRepository.updateBookingStatus(nonExistingBookingId, BookingStatus.booked);
    }

    @Test
    public void testGetBookingById() {
        final Guest guest = new Guest("John", "Doe", LocalDate.of(1980, 5, 10));
        final var bookingRequest = new BookingRequest(hotelId1, guest, roomId1, LocalDate.of(2022, 3, 1), LocalDate.of(2022, 3, 3),
            currency, payment);

        final var availableRoom = AvailableRoom.totalPricesOnly(1L, BigDecimal.valueOf(300), BigDecimal.valueOf(270), currency);
        final var bookingId = bookingRepository.createBooking(bookingRequest, availableRoom);

        Optional<BookingDTO> bookingDTO = bookingRepository.getBookingById(bookingId);


        assertTrue(bookingDTO.isPresent());
        assertEquals(bookingId, bookingDTO.get().bookingId);
        assertEquals("John", bookingDTO.get().guest.name);
        assertEquals("Doe", bookingDTO.get().guest.surname);
        assertEquals(BookingStatus.pending, bookingDTO.get().status);
        assertEquals(currency, bookingDTO.get().currency);
        // assert room
        assertEquals(roomId1.intValue(), bookingDTO.get().room.roomId);
        assertEquals(0, BigDecimal.valueOf(300).compareTo(bookingDTO.get().room.totalPriceAfterTax));
        assertEquals(0, BigDecimal.valueOf(270).compareTo(bookingDTO.get().room.totalPriceBeforeTax));
        assertEquals(currency, bookingDTO.get().room.currency);
        // assert payment
        assertEquals(payment.card_holder, bookingDTO.get().payment.card_holder);
        assertEquals(payment.card_number, bookingDTO.get().payment.card_number);
        assertEquals(payment.cvv, bookingDTO.get().payment.cvv);
        assertEquals(payment.expiry_month, bookingDTO.get().payment.expiry_month);
        assertEquals(payment.expiry_year, bookingDTO.get().payment.expiry_year);
    }
}
