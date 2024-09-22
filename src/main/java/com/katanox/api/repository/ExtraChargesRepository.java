package com.katanox.api.repository;

import java.math.BigDecimal;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import com.katanox.api.domain.charges.ExtraCharges;
import com.katanox.test.sql.enums.AppliedOn;
import com.katanox.test.sql.enums.ChargeType;
import com.katanox.test.sql.tables.ExtraChargesFlat;
import com.katanox.test.sql.tables.ExtraChargesPercentage;

import lombok.AllArgsConstructor;

@Repository
public class ExtraChargesRepository {

    private final DSLContext dsl;
    final ExtraChargesFlat flatCharges = ExtraChargesFlat.EXTRA_CHARGES_FLAT;
    final ExtraChargesPercentage percentageCharges = ExtraChargesPercentage.EXTRA_CHARGES_PERCENTAGE;

    public ExtraChargesRepository(final DSLContext dsl) {
        this.dsl = dsl;
    }

    public ExtraCharges getExtraCharges(final Long hotelId, final int numberOfNights) {

        final var flat = dsl.select(
                flatCharges.HOTEL_ID,
                // Sum one-time charges
                DSL.coalesce(DSL.sum(flatCharges.PRICE).filterWhere(flatCharges.CHARGE_TYPE.eq(ChargeType.once)), 0).add(
                // Sum per-night charges and multiply by number of nights
                DSL.coalesce(DSL.sum(flatCharges.PRICE.mul(numberOfNights)).filterWhere(flatCharges.CHARGE_TYPE.eq(ChargeType.per_night)), 0))
            ).from(flatCharges)
            .where(flatCharges.HOTEL_ID.eq(hotelId))
            .groupBy(flatCharges.HOTEL_ID)
            .fetchOneInto(FlatCharges.class);

        final var percentage = dsl.select(
                percentageCharges.HOTEL_ID,
                // sum all total_amount percentages
                DSL.coalesce(DSL.sum(percentageCharges.PERCENTAGE).filterWhere(percentageCharges.APPLIED_ON.eq(AppliedOn.total_amount)), 0),
                // sum all first night percentages
                DSL.coalesce(DSL.sum(percentageCharges.PERCENTAGE).filterWhere(percentageCharges.APPLIED_ON.eq(AppliedOn.first_night)), 0)
            ).from(percentageCharges)
            .where(percentageCharges.HOTEL_ID.eq(hotelId))
            .groupBy(percentageCharges.HOTEL_ID)
            .fetchOneInto(PercentageCharges.class);

        return new ExtraCharges(hotelId, flat.totalFlatCharges, percentage.firstNightAmountPercentage, percentage.totalAmountPercentage);
    }

    @AllArgsConstructor
    private static class FlatCharges{
        public Long hotelId;
        public BigDecimal totalFlatCharges;
    }

    @AllArgsConstructor
    private static class PercentageCharges{
        public Long hotelId;
        public BigDecimal totalAmountPercentage;
        public BigDecimal firstNightAmountPercentage;
    }

}
