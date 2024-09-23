package com.katanox.api.domain.search;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class SearchRequest {
    public LocalDate checkin;
    public LocalDate checkout;
    public long hotelId;
    public String currency;
}