package com.katanox.api.repository;

import static java.time.temporal.ChronoUnit.DAYS;

import java.time.LocalDate;
import java.util.List;

import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import com.katanox.api.domain.booking.AvailableRoom;
import com.katanox.test.sql.tables.Hotels;
import com.katanox.test.sql.tables.Prices;
import com.katanox.test.sql.tables.Rooms;

@Repository
public class AvailabilityRepository {

    private final DSLContext dsl;
    final Rooms room = Rooms.ROOMS;
    final Prices prices = Prices.PRICES;
    final Hotels hotels = Hotels.HOTELS;

    public AvailabilityRepository(final DSLContext dsl) {
        this.dsl = dsl;
    }

    public List<AvailableRoom> searchAvailableRooms(final Long hotelId, final LocalDate checkin, final LocalDate checkout,
        final String currency) {
        final var numberOfNights = (int) DAYS.between(checkin, checkout);

        if (numberOfNights == 0) {
            return List.of();
        }

        Condition whereCondition = room.HOTEL_ID.eq(hotelId)
            .and(prices.DATE.greaterOrEqual(checkin).and(prices.DATE.lessThan(checkout)))
            .and(prices.QUANTITY.gt(0))
            .and(prices.CURRENCY.equal(currency));

        return dsl.select(
                room.ID,
                // Total price after tax for all nights
                DSL.sum(prices.PRICE_AFTER_TAX),
                // First-night price after tax
                DSL.max(DSL.when(prices.DATE.eq(checkin), prices.PRICE_AFTER_TAX)),
                prices.CURRENCY
            )
            .from(room)
            .join(prices).on(room.ID.eq(prices.ROOM_ID))
            .where(whereCondition)
            .groupBy(room.ID, prices.CURRENCY)
            .having(DSL.count(prices.DATE).equal(numberOfNights))  // Make sure room has availability for all dates
            .fetch(r -> AvailableRoom.totalPriceAndFirstNightPriceAfterTax(r.value1(), r.value2(), r.value3(), r.value4()));
    }

    public List<AvailableRoom> getAvailabilityForBooking(final Long hotelId, final Long roomId, final LocalDate checkin, final LocalDate checkout,
                                                              final String currency) {
        final var numberOfNights = (int) DAYS.between(checkin, checkout);

        if (numberOfNights == 0) {
            return List.of();
        }

        Condition whereCondition = room.HOTEL_ID.eq(hotelId)
            .and(prices.DATE.greaterOrEqual(checkin).and(prices.DATE.lessThan(checkout)))
            .and(prices.QUANTITY.gt(0))
            .and(prices.CURRENCY.equal(currency))
            .and(room.ID.eq(roomId));

        return dsl.select(
                room.ID,
                // Total price after tax for all nights
                DSL.sum(prices.PRICE_AFTER_TAX),
                // Total price before tax for all nights
                DSL.sum(prices.PRICE_AFTER_TAX.div(DSL.val(1).add(hotels.VAT.divide(DSL.val(100))))),
                // First-night price after tax
                DSL.max(DSL.when(prices.DATE.eq(checkin), prices.PRICE_AFTER_TAX)),
                // First-night price before tax
                DSL.max(DSL.when(prices.DATE.eq(checkin), prices.PRICE_AFTER_TAX.div(DSL.val(1).add(hotels.VAT.divide(DSL.val(100)))))),
                prices.CURRENCY
            )
            .from(room)
            .join(prices).on(room.ID.eq(prices.ROOM_ID))
            .join(hotels).on(room.HOTEL_ID.eq(hotels.ID))  // Join hotels table to get VAT
            .where(whereCondition)
            .groupBy(room.ID, prices.CURRENCY)
            .having(DSL.countDistinct(prices.DATE).equal(numberOfNights))
            .fetchInto(AvailableRoom.class);
    }

}
