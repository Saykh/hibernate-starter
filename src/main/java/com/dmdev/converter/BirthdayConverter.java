package com.dmdev.converter;

import com.dmdev.entity.BirthDay;

import javax.persistence.AttributeConverter;
import javax.persistence.Convert;
import javax.persistence.Converter;
import java.sql.Date;
import java.util.Optional;

/* (4) */
@Converter(autoApply = true)
public class BirthdayConverter implements AttributeConverter<BirthDay, Date> {


    /*
        Когда из Java типа преобразовываем в SQL тип.
    */
    @Override
    public Date convertToDatabaseColumn(BirthDay attribute) {
        return Optional.ofNullable(attribute)
                .map(BirthDay::birthDate)
                .map(Date::valueOf) // Преобразовали в SQL.
                .orElse(null);
    }

    /*
        Наоборот, когда считываем типа.
    */
    @Override
    public BirthDay convertToEntityAttribute(Date dbData) {
        return Optional.ofNullable(dbData)
                .map(Date::toLocalDate) // Преобразовали в LocalDate
                .map(BirthDay::new)  // И в наш класс
                .orElse(null); // (3)
    }
}
