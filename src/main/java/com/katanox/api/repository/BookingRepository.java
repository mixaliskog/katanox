package com.katanox.api.repository;

import java.util.Optional;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import com.katanox.api.domain.booking.AvailableRoom;
import com.katanox.api.domain.booking.BookingDTO;
import com.katanox.api.domain.booking.BookingRequest;
import com.katanox.test.sql.enums.BookingStatus;
import com.katanox.test.sql.tables.BookingPayments;
import com.katanox.test.sql.tables.BookingRooms;
import com.katanox.test.sql.tables.Bookings;
import com.katanox.test.sql.tables.records.BookingPaymentsRecord;
import com.katanox.test.sql.tables.records.BookingRoomsRecord;

@Repository
public class BookingRepository {

    private final DSLContext dsl;
    private final Bookings bookings = Bookings.BOOKINGS;
    private final BookingPayments bookingPayments = BookingPayments.BOOKING_PAYMENTS;
    private final BookingRooms bookingRooms = BookingRooms.BOOKING_ROOMS;

    public BookingRepository(final DSLContext dsl) {
        this.dsl = dsl;
    }

    public Long createBooking(final BookingRequest booking, final AvailableRoom roomToBeReserved) {

        // create booking
        final var bookingId = dsl.insertInto(bookings)
            .set(bookings.HOTEL_ID, booking.hotelId)
            .set(bookings.GUEST_NAME, booking.guest.name)
            .set(bookings.GUEST_SURNAME, booking.guest.surname)
            .set(bookings.GUEST_DATE_OF_BIRTH, booking.guest.birthdate)
            .set(bookings.CHECKIN_DATE, booking.checkin)
            .set(bookings.CHECKOUT_DATE, booking.checkout)
            .set(bookings.STATUS, BookingStatus.pending)
            .set(bookings.CURRENCY, booking.currency)
            .returning(bookings.ID)
            .fetchOne()
            .getValue(bookings.ID, Long.class);

        // maybe move them to separate repositories
        //create booking room
        dsl.insertInto(bookingRooms)
            .set(bookingRooms.BOOKING_ID, bookingId)
            .set(bookingRooms.TOTAL_PRICE_BEFORE_TAX, roomToBeReserved.totalPriceBeforeTax)
            .set(bookingRooms.TOTAL_PRICE_AFTER_TAX, roomToBeReserved.totalPriceAfterTax)
            .set(bookingRooms.ROOM_ID, roomToBeReserved.roomId)
            .set(bookingRooms.HOTEL_ID, booking.hotelId)
            .set(bookingRooms.CURRENCY, roomToBeReserved.currency)
            .set(bookingRooms.CHECKIN_DATE, booking.checkin)
            .set(bookingRooms.CHECKOUT_DATE, booking.checkout)
            .execute();

        //create payment
        dsl.insertInto(bookingPayments)
            .set(bookingPayments.BOOKING_ID, bookingId)
            .set(bookingPayments.CARD_HOLDER, booking.payment.card_holder)
            .set(bookingPayments.CARD_NUMBER, booking.payment.card_number)
            .set(bookingPayments.CVV, booking.payment.cvv)
            .set(bookingPayments.EXPIRY_MONTH, booking.payment.expiry_month)
            .set(bookingPayments.EXPIRY_YEAR, booking.payment.expiry_year)
            .execute();

        // Return the reservation ID for confirmation
        return bookingId;

    }

    public boolean updateBookingStatus(final Long bookingId, final BookingStatus status) {
        final var bookingRecord = dsl.selectFrom(bookings)
            .where(bookings.ID.eq(bookingId))
            .fetchOne();

        if (bookingRecord == null) {
            throw new IllegalArgumentException("Booking not found.");
        }

        if (bookingRecord.getStatus() == status) {
            return false;
        }

        // Update the booking status
        dsl.update(bookings)
            .set(bookings.STATUS, status)
            .where(bookings.ID.eq(bookingId))
            .execute();

        return true;
    }

    public Optional<BookingDTO> getBookingById(final long bookingId) {
        return Optional.ofNullable(dsl.fetchOne(Bookings.BOOKINGS, Bookings.BOOKINGS.ID.eq(bookingId)))
            .map(bookingRecord -> {
                final BookingRoomsRecord bookingRoomsRecord = dsl.fetchOne(bookingRooms, bookingRooms.BOOKING_ID.eq(bookingId));
                final BookingPaymentsRecord bookingPaymentsRecord = dsl.fetchOne(bookingPayments, bookingPayments.BOOKING_ID.eq(bookingId));
                return BookingDTO.fromBookingRecord(bookingRecord, bookingRoomsRecord, bookingPaymentsRecord);
            });

    }

}
