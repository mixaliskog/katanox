package com.katanox.api.service;

import com.katanox.api.domain.booking.AvailableRoom;
import com.katanox.api.domain.charges.ExtraCharges;
import com.katanox.api.repository.ExtraChargesRepository;
import com.katanox.api.util.TestUtil;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;

import static com.katanox.api.util.TestUtil.hotelId1;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

@RunWith(Parameterized.class)
public class ChargeCalculationServiceParameterisedTest {

    @Parameterized.Parameter(0)
    public Long roomId;

    @Parameterized.Parameter(1)
    public BigDecimal totalPriceAfterTax;

    @Parameterized.Parameter(2)
    public BigDecimal totalPriceBeforeTax;

    @Parameterized.Parameter(3)
    public BigDecimal firstNightPriceAfterTax;

    @Parameterized.Parameter(4)
    public BigDecimal firstNightPriceBeforeTax;

    @Parameterized.Parameter(5)
    public String currency;

    @Parameterized.Parameter(6)
    public ExtraCharges extraCharges;

    @Parameterized.Parameter(7)
    public BigDecimal expectedAfterTaxPrice;

    @Parameterized.Parameter(8)
    public BigDecimal expectedBeforeTaxPrice;

    @Mock
    private ExtraChargesRepository extraChargesRepository;
    @InjectMocks
    private ChargeCalculationService chargeCalculationService;
    static final BigDecimal zero = BigDecimal.ZERO;

    @Parameterized.Parameters(name = "{index}: RoomId={0}, TotalAfterTax={1}, TotalBeforeTax={2}, ExpectedAfterTax={7}, ExpectedBeforeTax={8}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            // 300 + 50 + 300 * 10% + 100 * 5%
            // 350 + 30 + 5 = 385
            {1L, new BigDecimal("300"), zero, new BigDecimal("100"), zero, TestUtil.currency,
                new ExtraCharges(hotelId1, new BigDecimal("50"), new BigDecimal("5"), new BigDecimal("10")),
                new BigDecimal("385.00"), zero},
            // 300 + 50 + 300 * 0 + 100 * 5%
            // 350 + 5 = 355
            {2L, new BigDecimal("300"), zero, new BigDecimal("100"), zero, TestUtil.currency,
                new ExtraCharges(hotelId1, new BigDecimal("50"), new BigDecimal("5"), zero),
                new BigDecimal("355.00"), zero},
            // 300
            {3L, new BigDecimal("300"), zero, new BigDecimal("100"), zero, TestUtil.currency,
                new ExtraCharges(hotelId1, zero, zero, zero),
                new BigDecimal("300.00"), zero},
            // 400 + 60 + 400 * 10% + 150 * 5%
            // 400 + 60 + 40 + 7.5 = 507.5
            // Pretax:
            // 350 + 60 + 350 * 10% + 120 * 5%
            // 350 + 60 + 35 + 6 = 451
            {4L, new BigDecimal("400"), new BigDecimal("350"), new BigDecimal("150"), new BigDecimal("120"), TestUtil.currency,
                new ExtraCharges(hotelId1, new BigDecimal("60"), new BigDecimal("5"), new BigDecimal("10")),
                new BigDecimal("507.50"), new BigDecimal("451.00")},
            {4L, new BigDecimal("400"), new BigDecimal("350"), new BigDecimal("150"), new BigDecimal("120"), TestUtil.currency,
                new ExtraCharges(hotelId1, zero, new BigDecimal("5"), new BigDecimal("10")),
                new BigDecimal("447.50"), new BigDecimal("391.00")},
            {4L, new BigDecimal("400"), new BigDecimal("350"), new BigDecimal("150"), new BigDecimal("120"), TestUtil.currency,
                new ExtraCharges(hotelId1, zero, zero, new BigDecimal("10")),
                new BigDecimal("440.00"), new BigDecimal("385")},
            {4L, new BigDecimal("400"), new BigDecimal("350"), new BigDecimal("150"), new BigDecimal("120"), TestUtil.currency,
                new ExtraCharges(hotelId1, zero, zero, zero),
                new BigDecimal("400.00"), new BigDecimal("350.00")},
        });
    }

    @Test
    public void testApplyChargesToRoom() {
        extraChargesRepository = mock(ExtraChargesRepository.class);
        chargeCalculationService = new ChargeCalculationService(extraChargesRepository);

        AvailableRoom room = new AvailableRoom(roomId, totalPriceAfterTax, totalPriceBeforeTax,
            firstNightPriceAfterTax, firstNightPriceBeforeTax, currency);


        AvailableRoom result = chargeCalculationService.applyChargesToRoom(room, extraCharges, currency);

        // Assert the expected values
        assertEquals(0, expectedAfterTaxPrice.compareTo(result.totalPriceAfterTax));
        assertEquals(0, expectedBeforeTaxPrice.compareTo(result.totalPriceBeforeTax));
        assertEquals((long) roomId, result.roomId);
        assertEquals(currency, result.currency);
    }
}
