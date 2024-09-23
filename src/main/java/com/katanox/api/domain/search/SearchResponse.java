package com.katanox.api.domain.search;

import java.util.List;

import com.katanox.api.domain.booking.AvailableRoom;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SearchResponse {
    public long hotelId;
    public List<AvailableRoom> availableRooms;

    public static SearchResponse empty(final long hotelId) {
        return new SearchResponse(hotelId, List.of());
    }
}
