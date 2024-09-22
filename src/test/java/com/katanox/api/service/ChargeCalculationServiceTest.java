package com.katanox.api.service;

import static com.katanox.api.util.TestUtil.currency;
import static com.katanox.api.util.TestUtil.firstOfMarch;
import static com.katanox.api.util.TestUtil.guest;
import static com.katanox.api.util.TestUtil.hotelId1;
import static com.katanox.api.util.TestUtil.payment;
import static com.katanox.api.util.TestUtil.roomId1;
import static com.katanox.api.util.TestUtil.sixthOfMarch;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.katanox.api.domain.booking.AvailableRoom;
import com.katanox.api.domain.booking.BookingRequest;
import com.katanox.api.domain.charges.ExtraCharges;
import com.katanox.api.repository.ExtraChargesRepository;


public class ChargeCalculationServiceTest {
    @Mock
    private ExtraChargesRepository extraChargesRepository;
    @InjectMocks
    private ChargeCalculationService chargeCalculationService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testApplyChargesForBooking() {
        final BookingRequest request = new BookingRequest(hotelId1, guest, roomId1, firstOfMarch, sixthOfMarch, currency, payment);
        final AvailableRoom room = new AvailableRoom(roomId1,
            new BigDecimal("200"),
            new BigDecimal("100"),
            new BigDecimal("20"),
            new BigDecimal("10"),
            currency);
        final ExtraCharges charges = new ExtraCharges(hotelId1, new BigDecimal("10"), new BigDecimal("5"), new BigDecimal("10"));

        when(extraChargesRepository.getExtraCharges(hotelId1, 5)).
            thenReturn(charges);

        final AvailableRoom result = chargeCalculationService.applyExtraChargesForBooking(request, room);
        assertEquals(new BigDecimal("231.00"), result.totalPriceAfterTax);
        assertEquals(new BigDecimal("120.50"), result.totalPriceBeforeTax);
        assertEquals(1L, result.roomId);
        assertEquals(currency, result.getCurrency());

        verify(extraChargesRepository, times(1)).getExtraCharges(hotelId1, 5);
    }

    @Test
    public void testApplyChargesToAvailableRooms() {
        final AvailableRoom room = new AvailableRoom(roomId1,
            new BigDecimal("200"),
            BigDecimal.ZERO,
            new BigDecimal("20"),
            BigDecimal.ZERO,
            currency);

        final AvailableRoom room1 = new AvailableRoom(2,
            new BigDecimal("300"),
            BigDecimal.ZERO,
            new BigDecimal("30"),
            BigDecimal.ZERO,
            currency);

        final ExtraCharges charges = new ExtraCharges(hotelId1, new BigDecimal("9"), new BigDecimal("3"), new BigDecimal("10"));

        when(extraChargesRepository.getExtraCharges(hotelId1, 5)).
            thenReturn(charges);

        final List<AvailableRoom> result = chargeCalculationService.applyExtraChargesToAvailableRooms(hotelId1,
            firstOfMarch,
            sixthOfMarch,
            List.of(room, room1),
            currency);
        assertEquals(2, result.size());

        final AvailableRoom r1 = result.get(0);

        assertEquals(new BigDecimal("229.60"), r1.totalPriceAfterTax);
        assertEquals(0, r1.totalPriceBeforeTax.compareTo(BigDecimal.ZERO));
        assertEquals(1L, r1.roomId);
        assertEquals(currency, r1.getCurrency());

        final AvailableRoom r2 = result.get(1);

        assertEquals(new BigDecimal("339.90"), r2.totalPriceAfterTax);
        assertEquals(0, r2.totalPriceBeforeTax.compareTo(BigDecimal.ZERO));
        assertEquals(2L, r2.roomId);
        assertEquals(currency, r2.getCurrency());

        verify(extraChargesRepository, times(1)).getExtraCharges(hotelId1, 5);
    }
}
