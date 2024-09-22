package com.katanox.api.domain.booking;

import com.katanox.test.sql.tables.records.BookingPaymentsRecord;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Payment {
    public String card_holder;
    public String card_number;
    public String cvv;
    public String expiry_month;
    public String expiry_year;

    public static Payment fromDBRecord(final BookingPaymentsRecord record) {
        return new Payment(record.getCardHolder(), record.getCardNumber(), record.getCvv(), record.getExpiryMonth(), record.getExpiryYear());
    }
}
