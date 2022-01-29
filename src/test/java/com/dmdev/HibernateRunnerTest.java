package com.dmdev;

import com.dmdev.entity.BirthDay;
import com.dmdev.entity.Company;
import com.dmdev.entity.PersonalInfo;
import com.dmdev.entity.User;
import com.dmdev.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
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

                .personalInfo(PersonalInfo.builder()
                        .firstname("Mansur")
                        .birthDate(new BirthDay(LocalDate.of(1994, 7, 23)))
                        .build())
                .build();

        String sql = """
                INSERT
                INTO 
                %s
                (%s)
                VALUES
                (%s)
                """;


        String tableName = Optional.ofNullable(user.getClass().getAnnotation(Table.class))
                .map(tableAnnotation -> tableAnnotation.schema() + "." + tableAnnotation.name())
                .orElse(user.getClass().getName());



        Field[] declaredFields = user.getClass().getDeclaredFields();

        String columnNames = Arrays.stream(declaredFields)
                .map(field -> Optional.ofNullable(field.getAnnotation(Column.class))
                        .map(Column::name)
                        .orElse(field.getName()))
                .collect(Collectors.joining(", "));




        String columnsValues = Arrays.stream(declaredFields)
                .map(field -> "?")
                .collect(Collectors.joining(", "));

        System.out.println(sql.formatted(tableName, columnNames, columnsValues));


  
        Connection connection = null;

        PreparedStatement preparedStatement = connection.prepareStatement(sql.formatted(tableName,
                columnNames, columnsValues));

        for (Field declaredField : declaredFields) {
            declaredField.setAccessible(true);
            preparedStatement.setObject(1, declaredField.get(user));
        }

    }


    @Test
    void OneToMany() {

        try (SessionFactory sessionFactory = HibernateUtil.buildSessionFactory()) {
            Session session = sessionFactory.openSession();

            session.beginTransaction();

            Company company = session.get(Company.class, 1L);

            System.out.println();

            session.getTransaction().commit();

        }
    }


    @Test
    void addUserToNewCompany() {

        try (SessionFactory sessionFactory = HibernateUtil.buildSessionFactory()) {
            Session session = sessionFactory.openSession();

            session.beginTransaction();

            Company company = Company.builder()
                    .name("Facebook")
                    .build();

            User user = User.builder()
                    .username("Aska")
                    .build();

            company.addUser(user);

            session.save(company);


            session.getTransaction().commit();

        }


    }
}