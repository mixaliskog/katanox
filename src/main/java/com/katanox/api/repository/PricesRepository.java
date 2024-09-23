package com.katanox.api.repository;

import static java.time.temporal.ChronoUnit.DAYS;

import java.time.LocalDate;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import com.katanox.api.domain.booking.BookingDTO;
import com.katanox.api.domain.booking.BookingRequest;
import com.katanox.test.sql.tables.Prices;

@Repository
public class PricesRepository {

    private final DSLContext dsl;

    public PricesRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    public void reserveQuantityForBooking(BookingRequest bookingRequest) {
        updateRoomQuantityForBooking(bookingRequest.roomId, bookingRequest.checkin, bookingRequest.checkout, -1);
    }

    public void releaseQuantityForBooking(BookingDTO bookingDTO) {
        updateRoomQuantityForBooking(bookingDTO.room.roomId, bookingDTO.checkin, bookingDTO.checkout, 1);
    }

    private void updateRoomQuantityForBooking(final Long roomId, final LocalDate checkin, final LocalDate checkout,
        final int quantityToAdd) {
        final Prices prices = Prices.PRICES;
        final int numberOfDates = (int) DAYS.between(checkin, checkout);

        if (quantityToAdd < 0) {
            // only lock the rows if the booking is about to be reserved,
            // there should be no race condition with the cancellation
            acquireDatabaseLock(roomId, checkin, checkout, prices, numberOfDates);
        }

        dsl.update(prices)
            .set(prices.QUANTITY, prices.QUANTITY.add(quantityToAdd))
            .where(prices.ROOM_ID.eq(roomId))
            .and(prices.DATE.between(checkin, checkout.minusDays(1)))
            .execute();
    }

    private void acquireDatabaseLock(final Long roomId, final LocalDate checkin, final LocalDate checkout, final Prices prices, final int numberOfDates) {
        // Lock rows for the reservation period using `FOR UPDATE`
        final var lockedRows = dsl.select(prices.ROOM_ID, prices.QUANTITY)
            .from(prices)
            .where(prices.ROOM_ID.eq(roomId))
            .and(prices.DATE.between(checkin,
                checkout.minusDays(1)))  // Ensure dates fall between check-in and the night before check-out
            .and(prices.QUANTITY.gt(0))  // Ensure that quantity is greater than 0
            .forUpdate()  // Lock the rows for update
            .fetch();

        if (lockedRows.size() != numberOfDates) {
            throw new IllegalArgumentException("Room are no longer available for the selected dates.");
        }
    }

}
