package com.dmdev;

import com.dmdev.converter.BirthdayConverter;
import com.dmdev.entity.*;
import com.dmdev.util.HibernateUtil;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.table.TableRowSorter;
import java.sql.SQLException;
import java.time.LocalDate;

@Slf4j // private static final Logger log = LoggerFactory.getLogger(HibernateRunner.class);
public class HibernateRunner {




    public static void main(String[] args) throws SQLException {

        /*
            (1)
        */
        Company company = Company.builder()
                .name("Google")
                .build();


        User user = User.builder()
                .username("isaev_mv@mail.ru")
                .personalInfo(PersonalInfo.builder()
                        .firstname("Muslim")
                        .lastname("Isaev")
                        .birthDate(new BirthDay(LocalDate.of(1993, 4, 8)))
                        .build())
                .role(Role.ADMIN)
                .info("""
                        {
                            "name": "Muslim",
                            "id" : 3
                        }
                        """)

                /*
                    (2)
                */
                .company(company)
                .build();


        try (SessionFactory sessionFactory = HibernateUtil.buildSessionFactory()) {
            Session session1 = sessionFactory.openSession();
            try (session1) {
                Transaction transaction = session1.beginTransaction();

                /*
                    (3) Регистрируем в hibernate.cfg.xml, либо в нашем HibernateUtil классе.
                    (4) Сохраняем.

                    session1.save(company);
                    session1.save(user);
                */


                /*
                    В нашем PersistentContext будет две сущности.
                */


                User user1 = session1.get(User.class, 1L);


                /*
                    select

                    from
                    users user0_
                    inner join
                    company company1_
                    on user0_.company_id=company1_.id
                    where
                    user0_.id=?

                Поставили optional = false и добились inner join.

                А как сделать отдельный запрос на получение компании? Мы к этому ещё вернемся на
                проблеме n + 1.

                Но делается это так.
                Вот мы поставили fetch на Lazy. Но оп, теперь мы не видим запроса в company.
                Мы не получили компанию. Что ж такое?
                Смотрим дебаг. Компания установлена. Но мы видим не компанию, а какой-то Company$HibernateProxy....
                Это прокси класс, который создается динамически во время выполения, и на этапе компиляции
                его нет. Если открыть его, увидим, что он похож на обычный класс company, но ещё
                с каким-то полем interceptor. Благодаря прокси и их реализации у нас есть возможность инициализ-ть
                нашу компанию тогда, когда мы попросим поля у нашей сущности company.

                Hibernate, используя proxy, позволяет нам лениво инициал-ть сущность, если мы попросим об этом.

                https://habr.com/ru/company/otus/blog/578950/


                Короче, через Lazy и Optional мы оптимизируем работу наших запросов.





                */


                session1.getTransaction().commit();


            }

        }
    }
}
