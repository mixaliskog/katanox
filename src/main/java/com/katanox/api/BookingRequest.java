package com.katanox.api;

public class BookingRequest {
    public long hotelId;
    public long roomId;
    public long price;
    public String currency;
    public Guest guest;
    public Payment payment;
}
