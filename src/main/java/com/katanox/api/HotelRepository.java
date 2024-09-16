package com.katanox.api;

import com.katanox.test.sql.tables.Hotels;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class HotelRepository {

    @Autowired
    private DSLContext dsl;

    public void insertHotel() {
        var hotel = Hotels.HOTELS;
        dsl.insertInto(hotel, hotel.NAME, hotel.ROOMS)
                .values("fake", 1)
                .execute();
    }
}
