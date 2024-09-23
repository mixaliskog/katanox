package com.katanox.api.domain.booking;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.katanox.test.sql.tables.records.BookingRoomsRecord;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class AvailableRoom {
    public long roomId;
    @JsonIgnore
    public BigDecimal totalPriceAfterTax;
    @JsonIgnore
    public BigDecimal totalPriceBeforeTax;
    @JsonIgnore
    public BigDecimal firstNightPriceAfterTax;
    @JsonIgnore
    public BigDecimal firstNightPriceBeforeTax;
    public String currency;

    public BigDecimal getPrice() {
        return totalPriceAfterTax;
    }

    public Long getRoomId() {
        return roomId;
    }

    public String getCurrency() {
        return currency;
    }

    public AvailableRoom(final long roomId, final BigDecimal totalPriceAfterTax, final BigDecimal totalPriceBeforeTax,
        final BigDecimal firstNightPriceAfterTax,
        final BigDecimal firstNightPriceBeforeTax, final String currency) {
        this.roomId = roomId;
        this.totalPriceAfterTax = totalPriceAfterTax;
        this.totalPriceBeforeTax = totalPriceBeforeTax.setScale(2, RoundingMode.HALF_EVEN);
        this.firstNightPriceAfterTax = firstNightPriceAfterTax;
        this.firstNightPriceBeforeTax = firstNightPriceBeforeTax.setScale(2, RoundingMode.HALF_EVEN);
        this.currency = currency;
    }

    public static AvailableRoom totalPriceAfterTax(final long roomId, final BigDecimal totalPriceAfterTax, final String currency) {
        return new AvailableRoom(roomId, totalPriceAfterTax, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, currency);
    }

    public static AvailableRoom totalPricesOnly(final long roomId, final BigDecimal totalPriceAfterTax, final BigDecimal totalPriceBeforeTax,
        final String currency) {
        return new AvailableRoom(roomId, totalPriceAfterTax, totalPriceBeforeTax, BigDecimal.ZERO, BigDecimal.ZERO, currency);
    }

    public static AvailableRoom totalPriceAndFirstNightPriceAfterTax(final long roomId, final BigDecimal totalPriceAfterTax,
        final BigDecimal firstNightPriceAfterTax,
        final String currency) {
        return new AvailableRoom(roomId, totalPriceAfterTax, BigDecimal.ZERO, firstNightPriceAfterTax, BigDecimal.ZERO, currency);
    }

    public static AvailableRoom fromDBRecord(final BookingRoomsRecord record) {
        return AvailableRoom.totalPricesOnly(record.getRoomId(), record.getTotalPriceAfterTax(), record.getTotalPriceBeforeTax(), record.getCurrency());
    }
}
