package com.dmdev;

import com.dmdev.converter.BirthdayConverter;
import com.dmdev.entity.BirthDay;
import com.dmdev.entity.PersonalInfo;
import com.dmdev.entity.Role;
import com.dmdev.entity.User;
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
                .build();
        log.info("User entity is in transient state, object: {}", user);


        try (SessionFactory sessionFactory = HibernateUtil.buildSessionFactory()) {
            Session session1 = sessionFactory.openSession();
            try (session1) {
                Transaction transaction = session1.beginTransaction();
                log.trace("Transaction is created, {}", transaction);

                session1.save(user);
                log.trace("User is in persistent state: {}, session {}", user, session1);

                /*
                    Окей, мы сохранили. Теперь как получить его по такому сложному ключу?
                */


                session1.getTransaction().commit();

                log.trace("User is in detached state: {}, session is closed {}", user, session1);


            } catch (Exception e) {
                log.error("Exception occurred", e);
                throw e; // Пробросим дальше, что легло приложение.
            }


            try (Session session2 = sessionFactory.openSession()) {
                session2.beginTransaction();

                /*
                    Билдим на ключ. Теперь получаем класс и передаем в get не примитив, а наш сложный
                    первичный ключ.
                    На практике не стоит использовать их, лучше IDENTITY, когда таблица сама отвечает за
                    простой синтетический идентификатор.


                 */


                PersonalInfo personalInfo = PersonalInfo.builder()
                        .firstname("Albina")
                        .lastname("Edilova")
                        .birthDate(new BirthDay(LocalDate.of(1993, 4, 8)))
                        .build();



                User userGet = session2.get(User.class, personalInfo);
            }
        }

    }
}
