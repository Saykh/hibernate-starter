package com.dmdev.entity;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public record BirthDay(LocalDate birthDate) {

    /*
        Вот у нас класс, который Hibernate не знает как преобразовывать в SQL.
        Так поможем ему!
    */


    /*
        Автоматом определим возраст человека - дата его рождаения и текущая дата.
    */
    public long geAge() {
        return ChronoUnit.YEARS.between(birthDate, LocalDate.now());
    }
}
