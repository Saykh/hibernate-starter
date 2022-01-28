package com.dmdev.entity;

import com.dmdev.converter.BirthdayConverter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embeddable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Embeddable // Укажем, что это встраиваемый компонент.

public class PersonalInfo {


    private String firstname;
    private String lastname;

    private BirthDay birthDate;
}
