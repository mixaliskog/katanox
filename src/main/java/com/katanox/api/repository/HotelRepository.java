package com.katanox.api.repository;

import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.katanox.test.sql.tables.Hotels;

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
