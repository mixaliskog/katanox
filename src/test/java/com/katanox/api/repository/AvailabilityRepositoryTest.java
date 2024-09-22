package com.katanox.api.repository;

import static com.katanox.api.util.TestUtil.hotelId2;
import static com.katanox.api.util.TestUtil.roomId1;
import static com.katanox.api.util.TestUtil.secondOfMarch;
import static com.katanox.api.util.TestUtil.seventhOfMarch;
import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

import static com.katanox.api.util.TestUtil.currency;
import static com.katanox.api.util.TestUtil.hotelId1;
import static com.katanox.api.util.TestUtil.firstOfMarch;
import static com.katanox.api.util.TestUtil.sixthOfMarch;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.jooq.DSLContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jooq.JooqTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.katanox.api.domain.booking.AvailableRoom;
import com.katanox.test.sql.tables.Prices;

@JooqTest(
    properties = {
        "spring.test.database.replace=none"
    }
)
@RunWith(SpringRunner.class)
public class AvailabilityRepositoryTest {

    @Autowired
    DSLContext dsl;

    private AvailabilityRepository repository;

    @Before
    public void setUp() {
        this.repository = new AvailabilityRepository(dsl);
    }

    @Test
    public void testAvailability_HappyPath() {
        // for hotel 1 there are 2 rooms available
        // r1 has 100, 110, 110, 100, 130 = 550 total
        // r2 has 100, 150, 200, 100, 110 = 660 total
        // both rooms have availability for 5 days
        final List<AvailableRoom> availableRooms = repository.searchAvailableRooms(hotelId1, firstOfMarch, sixthOfMarch, currency);
        Assert.assertEquals(2, availableRooms.size());

        final AvailableRoom r1 = availableRooms.get(0);
        assertEquals(1L, r1.roomId);
        assertEquals(new BigDecimal("550"), r1.totalPriceAfterTax);
        assertEquals(new BigDecimal("100"), r1.firstNightPriceAfterTax);
        assertEquals(currency, r1.currency);

        final AvailableRoom r2 = availableRooms.get(1);
        assertEquals(2L, r2.roomId);
        assertEquals(new BigDecimal("660"), r2.totalPriceAfterTax);
        assertEquals(new BigDecimal("100"), r2.firstNightPriceAfterTax);
        assertEquals(currency, r2.currency);
    }

    @Test
    public void getRoomsForBooking() {
        // for hotel 1 there are 2 rooms available
        // r1 has 100, 110, 110, 100, 130 = 550 total
        // r2 has 100, 150, 200, 100, 110 = 660 total
        // both rooms have availability for 5 days
        // vat is 20 for hotel1 so pretax is calculated (total_amount/(1 + 0.20))
        // vat is 30 for hotel2 so pretax is calculated (total_amount/(1 + 0.30))
        final List<AvailableRoom> availableRooms = repository.getAvailabilityForBooking(hotelId1, roomId1, firstOfMarch, sixthOfMarch, currency);
        Assert.assertEquals(1, availableRooms.size());

        final AvailableRoom r1 = availableRooms.get(0);
        assertEquals(1L, r1.roomId);
        assertEquals(new BigDecimal("550"), r1.totalPriceAfterTax);
        assertEquals(new BigDecimal("458.33"), r1.totalPriceBeforeTax);
        assertEquals(new BigDecimal("100"), r1.firstNightPriceAfterTax);
        assertEquals(new BigDecimal("83.33"), r1.firstNightPriceBeforeTax);
        assertEquals(currency, r1.currency);

        final AvailableRoom r2 = repository.getAvailabilityForBooking(hotelId1, 2L, firstOfMarch, sixthOfMarch, currency).get(0);
        assertEquals(2L, r2.roomId);
        assertEquals(new BigDecimal("660"), r2.totalPriceAfterTax);
        assertEquals(new BigDecimal("550.00"), r2.totalPriceBeforeTax);
        assertEquals(new BigDecimal("100"), r2.firstNightPriceAfterTax);
        assertEquals(new BigDecimal("83.33"), r2.firstNightPriceBeforeTax);
        assertEquals(currency, r2.currency);
    }

    @Test
    public void testAvailability_HappyPath_singleNight() {
        // for hotel 1 there are 2 rooms available
        // r1 has 100
        // r2 has 100
        // both rooms have availability for 1 day
        final List<AvailableRoom> availableRooms = repository.searchAvailableRooms(hotelId1, firstOfMarch, secondOfMarch, currency);
        Assert.assertEquals(2, availableRooms.size());

        final AvailableRoom r1 = availableRooms.get(0);
        assertEquals(1L, r1.roomId);
        assertEquals(new BigDecimal("100"), r1.totalPriceAfterTax);
        assertEquals(new BigDecimal("100"), r1.firstNightPriceAfterTax);
        assertEquals(currency, r1.currency);

        final AvailableRoom r2 = availableRooms.get(1);
        assertEquals(2L, r2.roomId);
        assertEquals(new BigDecimal("100"), r2.totalPriceAfterTax);
        assertEquals(new BigDecimal("100"), r2.firstNightPriceAfterTax);
        assertEquals(currency, r2.currency);
    }

    @Test
    public void decreaseIncreaseQuantity() {
        final List<AvailableRoom> availableRooms = repository.searchAvailableRooms(hotelId2, sixthOfMarch, seventhOfMarch, currency);
        assertEquals(1, availableRooms.size());
        final AvailableRoom r4 = availableRooms.get(0);
        assertEquals(4, r4.roomId);

        //same for booking query
        final List<AvailableRoom> availableRoomsPretax = repository.getAvailabilityForBooking(hotelId2, 4L, sixthOfMarch, seventhOfMarch, currency);
        assertEquals(1, availableRoomsPretax.size());
        final AvailableRoom r4pre = availableRoomsPretax.get(0);
        assertEquals(4, r4pre.roomId);

        // the quantity is 6, so we need to reduce it by 6
        adjustRoomQuantity(4, sixthOfMarch, -6);

        // we should not see any result now
        assertTrue(repository.searchAvailableRooms(hotelId2, sixthOfMarch, seventhOfMarch, currency).isEmpty());
        assertTrue(repository.getAvailabilityForBooking(hotelId2, 4L, sixthOfMarch, seventhOfMarch, currency).isEmpty());

        //increase the quantity again
        adjustRoomQuantity(4, sixthOfMarch, 6);

        // we should see the results back now
        assertFalse(repository.searchAvailableRooms(hotelId2, sixthOfMarch, seventhOfMarch, currency).isEmpty());
        assertFalse(repository.getAvailabilityForBooking(hotelId2, 4L, sixthOfMarch, seventhOfMarch, currency).isEmpty());
    }

    @Test
    public void testAvailability_sameDay() {
        final var availableRooms = repository.searchAvailableRooms(hotelId1, firstOfMarch, firstOfMarch, currency);
        final var availableRoomsPreTax = repository.getAvailabilityForBooking(hotelId1, roomId1, firstOfMarch, firstOfMarch, currency);
        Assert.assertEquals(0, availableRooms.size());
        Assert.assertEquals(0, availableRoomsPreTax.size());
    }

    @Test
    public void testAvailability_invalidCurrency() {
        final var availableRooms = repository.searchAvailableRooms(hotelId1, firstOfMarch, firstOfMarch, "USD");
        final var availableRoomsPreTax = repository.getAvailabilityForBooking(hotelId1, roomId1, firstOfMarch, firstOfMarch, "USD");
        Assert.assertEquals(0, availableRooms.size());
        Assert.assertEquals(0, availableRoomsPreTax.size());
    }

    @Test
    public void testAvailability_HappyPath_filterOutPartiallyAvailableRoom() {
        // hotel 2 has 2 rooms
        // r3: 130, 160, 250, 100, <>
        // r4: 100, 110, 110, 110, 140 = 570
        // r3 should be filtered out since its not available for the last day
        final List<AvailableRoom> availableRooms = repository.searchAvailableRooms(hotelId2, secondOfMarch, seventhOfMarch, currency);
        Assert.assertEquals(1, availableRooms.size());
        final AvailableRoom r4 = availableRooms.get(0);
        assertEquals(4L, r4.roomId);
        assertEquals(new BigDecimal("570"), r4.totalPriceAfterTax);
        assertEquals(new BigDecimal("100"), r4.firstNightPriceAfterTax);
        assertEquals(currency, r4.currency);

        // if we decrease one day we should see both rooms come back
        // r3: 130, 160, 250, 100 = 640
        // r4: 100, 110, 110, 110 = 430
        final List<AvailableRoom> availableRooms2 = repository.searchAvailableRooms(hotelId2, secondOfMarch, sixthOfMarch, currency);
        Assert.assertEquals(2, availableRooms2.size());
        final AvailableRoom r3 = availableRooms2.get(0);
        assertEquals(3L, r3.roomId);
        assertEquals(new BigDecimal("640"), r3.totalPriceAfterTax);
        assertEquals(0, BigDecimal.ZERO.compareTo(r3.totalPriceBeforeTax));
        assertEquals(new BigDecimal("130"), r3.firstNightPriceAfterTax);
        assertEquals(0, BigDecimal.ZERO.compareTo(r3.firstNightPriceBeforeTax));
        assertEquals(currency, r3.currency);

        final AvailableRoom r4_updated = availableRooms2.get(1);
        assertEquals(4L, r4_updated.roomId);
        assertEquals(new BigDecimal("430"), r4_updated.totalPriceAfterTax);
        assertEquals(new BigDecimal("100"), r4_updated.firstNightPriceAfterTax);
        assertEquals(currency, r4_updated.currency);
    }

    void adjustRoomQuantity(final long roomId, final LocalDate date, final int adjustment) {
        final Prices prices = Prices.PRICES;

        dsl.update(prices)
            .set(prices.QUANTITY, prices.QUANTITY.add(adjustment))
            .where(prices.ROOM_ID.eq(roomId)
                .and(prices.DATE.eq(date)))
            .execute();
    }

}
