package com.katanox.api.repository;

import static com.katanox.api.util.TestUtil.bookingId;
import static com.katanox.api.util.TestUtil.currency;
import static com.katanox.api.util.TestUtil.firstOfMarch;
import static com.katanox.api.util.TestUtil.guest;
import static com.katanox.api.util.TestUtil.hotelId1;
import static com.katanox.api.util.TestUtil.payment;
import static com.katanox.api.util.TestUtil.roomId1;
import static com.katanox.api.util.TestUtil.sixthOfMarch;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;

import org.jooq.DSLContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jooq.JooqTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.katanox.api.domain.booking.AvailableRoom;
import com.katanox.api.domain.booking.BookingRequest;
import com.katanox.test.sql.tables.Prices;

@JooqTest(
    properties = {
        "spring.test.database.replace=none"
    }
)
@RunWith(SpringRunner.class)
public class PricesRepositoryTest {

    @Autowired
    DSLContext dsl;

    private PricesRepository pricesRepository;
    final Prices prices = Prices.PRICES;

    @Before
    public void setup() {
        this.pricesRepository = new PricesRepository(dsl);
    }

    @Test
    public void testReserveReleaseQuantityForBooking() {
        final BookingRequest bookingRequest = new BookingRequest(hotelId1, guest, roomId1, firstOfMarch, sixthOfMarch, currency, payment);
        // Before reservation, check the initial quantity
        assertQuantityOfRooms(2);

        pricesRepository.reserveQuantityForBooking(bookingRequest);
        assertQuantityOfRooms(1);

        pricesRepository.releaseQuantityForBooking(bookingRequest.toBookingDTO(bookingId,
            AvailableRoom.totalPricesOnly(roomId1, BigDecimal.TEN, BigDecimal.TEN, currency)));
        assertQuantityOfRooms(2);
    }

    @Test
    public void testReleaseQuantityForBooking_NotAvailable() {
        final BookingRequest bookingRequest = new BookingRequest(hotelId1, guest, roomId1, firstOfMarch, sixthOfMarch, currency, payment);
        // Before reservation, check the initial quantity
        assertQuantityOfRooms(2);
        pricesRepository.reserveQuantityForBooking(bookingRequest);
        pricesRepository.reserveQuantityForBooking(bookingRequest);
        assertQuantityOfRooms(0);
        try {
            pricesRepository.reserveQuantityForBooking(bookingRequest);
        } catch (final IllegalArgumentException e) {
            assertEquals("Room are no longer available for the selected dates.", e.getMessage());
        }

        pricesRepository.releaseQuantityForBooking(bookingRequest.toBookingDTO(bookingId,
            AvailableRoom.totalPricesOnly(roomId1, BigDecimal.TEN, BigDecimal.TEN, currency)));
        pricesRepository.releaseQuantityForBooking(bookingRequest.toBookingDTO(bookingId,
            AvailableRoom.totalPricesOnly(roomId1, BigDecimal.TEN, BigDecimal.TEN, currency)));
        assertQuantityOfRooms(2);
    }

    public void assertQuantityOfRooms(final Integer expectedQuantity) {
        dsl.select(prices.QUANTITY)
            .from(prices)
            .where(prices.ROOM_ID.eq(roomId1))
            .and(prices.DATE.between(firstOfMarch, sixthOfMarch.minusDays(1)))
            .forEach(quantity -> assertEquals(expectedQuantity, quantity.value1()));
    }

}
