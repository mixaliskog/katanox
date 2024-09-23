package com.katanox.api.domain.charges;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ExtraCharges {
    public long hotelId;
    public BigDecimal flatCharges;
    public BigDecimal firstNightAmountPercentage;
    public BigDecimal totalAmountPercentage;
}
