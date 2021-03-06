package com.dmdev.entity;

import com.dmdev.converter.BirthdayConverter;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.time.LocalDate;

/*
    Для ORM нам нужны сущности, которые мы будем проецировать на таблицы в БД.

            Чтобы класс стал Entity, данная сущность должна быть POJO (Plain Old Java object), а для этого должны быть \
            соблюдены следующие условия:

            1) Entity класс должен быть помечен аннотацией Entity или описан в XML файле.

            2) Entity класс должен быть классом верхнего уровня (top-level class).

            3) Entity класс не может быть enum или интерфейсом.

            4) Поля Entity класс должны быть напрямую доступны только методам самого Entity класса и не должны быть
            напрямую доступны другим классам, использующим этот Entity. Такие классы должны обращаться только к методам
            (getter/setter методам или другим методам бизнес-логики в Entity классе

            5) Entity класс не может содержать финальные поля или методы, если они участвуют в маппинге.

            6) Entity класс должен содержать первичный ключ, то есть атрибут или группу атрибутов которые уникально
            определяют запись этого Entity класса в базе данных.

            7) Если объект Entity класса будет передаваться по значению как отдельный объект (detached object),
            например через удаленный интерфейс (through a remote interface), он так же должен реализовывать Serializable
            интерфейс.

            8) Сущность не должна быть immutable, то есть сам класс и его поля не могут быть final, ведь Hibernate
            меняет их в своем жизненном цикле, а класс не может быть final потому, что Hibernate работает с прокси, и этот
            прокси работает по принципу CGLIB, то есть создаёт наследника от нашего класса.

            9) Обязательно должен быть public или protected конструктор без параметров, ибо Hibernate использует
            REFLECTION API для создания сущностей и последующей инициализации полей через сеттеры или напрямую через
            REFLECTION API.



*/


/* Аннотация из JPA. Мы ею говорим, что эта Hibernate сущность. */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

/* (6) */
@TypeDef(name = "dmdev", typeClass = JsonBinaryType.class)

/* У нас название сущности отличается от названия таблицы в БД, поэтому используем данную аннотацию. */
@Table(name = "users")

public class User {

    /* Аннотация из JPA.Каждая сущность в Hibernate должна иметь первичный ключ. И тип идент-ра должен быть
    Serializable. К этому ещё вернемся. */

    @Id
    private String username;

    private String firstname;
    private String lastname;

    /* (2) */
    @Column(name = "birth_date")
    /* private LocalDate birthDate; */

    /* (3) Скажем Hibernate использовать тут наш конвертер. */
    @Convert(converter = BirthdayConverter.class)
    private BirthDay birthDate;


    /*
        Убираем использование ORDINAL, который стоит по умолчанию. Ставим текстовое представление.
        Ordinal not good, изменим порядок в enum - всё сломается.
    */
    @Enumerated(EnumType.STRING)
    private Role role;



    /*
        Как и в случае с Converter, нужно указать этот новый тип и зарегистрировать в Hibernate.
        Укажем полный путь до JsonBinaryType.
        Но этого недостаточно, мы должны зарегистрировать его ещё в конфигурации (5).
    */
    /*
        @Type(type =  "com.vladmihalcea.hibernate.type.json.JsonBinaryType")
        Но можно указать иначе, ведь у JsonBinaryType есть метод getName, который он переопределяет
        от Type.
        @Type(type = "jsonb")

        Можно ещё над классом использовать специальную аннотацию (6).
    */
    @Type(type = "dmdev")
    private String info;
}
