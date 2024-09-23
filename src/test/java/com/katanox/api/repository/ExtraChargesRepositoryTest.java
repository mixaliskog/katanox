package com.katanox.api.repository;

import static com.katanox.api.util.TestUtil.hotelId1;
import static com.katanox.api.util.TestUtil.hotelId2;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.math.BigDecimal;

import org.jooq.DSLContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jooq.JooqTest;
import org.springframework.test.context.junit4.SpringRunner;

@JooqTest(
    properties = {
        "spring.test.database.replace=none"
    }
)
@RunWith(SpringRunner.class)
public class ExtraChargesRepositoryTest {

    @Autowired
    DSLContext dsl;

    private ExtraChargesRepository repository;

    @Before
    public void setUp() {
        this.repository = new ExtraChargesRepository(dsl);
    }

    @Test
    public void testCalculateFlatCharges_OnceAndPerNightCharges() {
        // Hotel1: once 25, per night 5
        final var extraCharges = repository.getExtraCharges(hotelId1, 3);
        assertNotNull(extraCharges);
        assertEquals(1L, extraCharges.hotelId);
        // 25 once and 3*5 = 15 -> 40 total
        assertEquals(new BigDecimal("40"), extraCharges.flatCharges);

        final var extraCharges2 = repository.getExtraCharges(hotelId1, 2);
        assertNotNull(extraCharges2);
        assertEquals(1L, extraCharges2.hotelId);
        // 25 once and 2*5 = 10 -> 35 total
        assertEquals(new BigDecimal("35"), extraCharges2.flatCharges);
    }

}
