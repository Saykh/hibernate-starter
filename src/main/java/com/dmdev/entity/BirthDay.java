package com.dmdev.entity;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record BirthDay(LocalDate birthDate) {




    public long geAge() {
        return ChronoUnit.YEARS.between(birthDate, LocalDate.now());
    }
}
