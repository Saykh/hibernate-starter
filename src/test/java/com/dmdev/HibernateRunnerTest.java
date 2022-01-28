package com.dmdev;

import com.dmdev.entity.BirthDay;
import com.dmdev.entity.User;
import org.junit.jupiter.api.Test;

import javax.persistence.Column;
import javax.persistence.Table;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class HibernateRunnerTest {

    /*
        Как объект Session под капотом формирует запрос через Reflection API.
    */


    @Test
    void checkReflectionApi() throws SQLException, IllegalAccessException {

        User user = User.builder()
                .username("edilov_st@mail.ru")
                .firstname("Saykhan")
                .lastname("Edilov")
                .birthDate(new BirthDay(LocalDate.of(1994, 7, 23)))
                .build();

        String sql = """
                INSERT
                INTO 
                %s
                (%s)
                VALUES
                (%s)
                """;

        /*
               TableName - динамическое составляющее первого значения.
               Если есть аннотация Table над нашей сущностью, возьми её, трансформируй в вид => схема.имятаблицы.
               Иначе же просто возьми имя таблицы.
        */
        String tableName = Optional.ofNullable(user.getClass().getAnnotation(Table.class))
                .map(tableAnnotation -> tableAnnotation.schema() + "." + tableAnnotation.name())
                .orElse(user.getClass().getName());


        /*
            Получим все поля и их названия.
        */

        Field[] declaredFields = user.getClass().getDeclaredFields();

        String columnNames =  Arrays.stream(declaredFields)
                .map(field -> Optional.ofNullable(field.getAnnotation(Column.class))
                .map(Column::name)
                .orElse(field.getName()))
                .collect(Collectors.joining(", "));


        /*
            Передаем поля вместо ? знаков.
        */

        String columnsValues = Arrays.stream(declaredFields)
                .map(field -> "?")
                .collect(Collectors.joining(", "));

        System.out.println(sql.formatted(tableName, columnNames, columnsValues));


        /*
            Теперь остался preparedStatement. Засунуть туда этот SQL и циклом пробежаться по каждому полю.
        */

        Connection connection = null;

        PreparedStatement preparedStatement = connection.prepareStatement(sql.formatted(tableName,
                columnNames, columnsValues));

        for (Field declaredField : declaredFields) {
            declaredField.setAccessible(true);
            preparedStatement.setObject(1, declaredField.get (user));
        }

    }






}