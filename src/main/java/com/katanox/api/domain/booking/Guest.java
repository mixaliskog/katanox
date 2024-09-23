package com.katanox.api.domain.booking;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Guest {
    public String name;
    public String surname;
    public LocalDate birthdate;
}
