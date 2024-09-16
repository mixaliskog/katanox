package com.katanox.api;

public class BookingDTO {

    public long hotelId;
    public long roomId;
    public long price_before_tax;
    public long price_after_tax;
    public String currency;
    public Guest guest;
    public Payment payment;
}
