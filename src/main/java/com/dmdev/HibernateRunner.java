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

    /*
        Будем видеть сообщение в каком классе произошёл message. Поэтому и нужно создавать logger
        в каждом отдельном классе.
    */


    public static void main(String[] args) throws SQLException {


        /*
            1. Вот мы создали сущность. Она находится в состоянии Transient по отношению к каждой сессии.

        */
        User user = User.builder()
                .username("edilova_at@mail.ru")
                .personalInfo(PersonalInfo.builder()
                        .firstname("Albina")
                        .lastname("Edilova")
                        .birthDate(new BirthDay(LocalDate.of(1993, 4, 8)))
                        .build())
                .role(Role.ADMIN)
                .info("""
                            {
                                "name": "Albina",
                                "id" : 2
                            }
                            """)
                .build();
        log.info("User entity is in transient state, object: {}", user);



        try (SessionFactory sessionFactory = HibernateUtil.buildSessionFactory()) {
            Session session1 = sessionFactory.openSession();
            try (session1) {
                Transaction transaction = session1.beginTransaction();
                log.trace("Transaction is created, {}", transaction);

                /*
                    2. Вот мы сохранили сущность. И в этой точке юзер будет в persistence состоянии
                    по отношению к session1 и всё ещё в состоянии transient по отношению к session2.


                */

                session1.save(user);

                log.trace("User is in persistent state: {}, session {}", user, session1);





                /*
                    3. Вот мы "закрываем" сессию. В принципе, уже session1 не существует, но если бы она была и
                    мы вызывали метод close, то наш юзер был бы в detached состоянии по отношению к session1, но
                    всё ещё в состоянии transient по отношению к session2.

                */
                session1.getTransaction().commit();
                log.trace("User is in detached state: {}, session is closed {}", user, session1);


            } catch (Exception e) {
                log.error("Exception occurred", e);
                throw e; // Пробросим дальше, что легло приложение.
            }



            try (Session session2 = sessionFactory.openSession()) {
                session2.beginTransaction();


                /*                    4. Сначала произойдет get метод для того, чтобы "проассоциировать" данную сущность с session2

                    попадает в Persistent), а потом только метод delete. Потому что метод delete переводит состояние
                    сущности из Persistent в Remove.

                    session2.delete(user);

                */



                /*
                    5. Вот тут у user lastname Sauron (но не в БД). Но так как данный юзер ещё не проассоциирован в
                    session2, то есть его нет в Persistence. Но как только мы делаем refresh происходит запрос в БД и мы
                    все изменения из БД накладываем на юзера. В PersistentContext появился юзер Edilov и теперь наш юзер
                    опять Edilov, а не Sauron.
                    Короче, под капотом произошло следующее:
                    1. у session2 вызвали метод get и получили нашего свежего юзера.
                    User freshUser = session2.get(User.class, user.getUsername());
                    2. устанавливаем в нашего юзера все наши поля из свежего юезра.
                    user.setFirstname(freshUser.getFirstname());
                    .........

                    То есть отправляем запрос в БД, получаем свежие данные и синхронизирует эти данные с нашим
                    пользователем, устанавливая данные из БД.

                    user.setLastname("Sauron");
                    session2.refresh(user);

                    Увидим Saykhan Frodo.

                */



                /*
                    6. Merge поход на refresh, но работает наоборот. То есть наши поля в юзере главнее, чем те, что в
                    БД. Поэтому он точно так же в БД, создает сущность на основании этих данных и устанавливает у этого
                    нового юзера данные из юзера, которые мы передали в метод merge.
                    Тут уже увидим Sauron.

                    user.setLastname("Sauron");
                    session2.merge(user);

                    Увидим Saykhan Sauron, а не Saykhan Frodo.
                */


                /*
                    Кстати, хоть метод UPDATE и выполнился, но у Hibernate отложенная отправка загрузка - он максимально
                    оттягивает момент открытия транзакции и начать общение с БД - это всё для оптимизации, чтоб мы могли
                    собрать больше запросов и отправить их хором, поэтому запрос выполнится тогда, когда мы закомитим
                    транзакцию, либо закроем нашу сессию.

                    Метод UPDATE бросает исключение, если такого юзера нет.
                    Но есть метод SAVEorUPDATE, который не бросит исключение, а создаст юзера.

                    Всё вертится вокруг идентификатора.

                    Кстати, в случае с DELETE никакого исключения не будет, если удаляемой сущности нет.

                    Опять же, работает отложенная отправка запроса -> сначала срабатывает select -> Hibernate
                    смотрим есть ли такая сущность, которую нужно удалить / обновить -> то есть делать ли последующий
                    запрос на удаление / обновление в какой-то момент до закрытия транзакции.


                    GET -> получение сущности по идентификатору (тоже).

                    session.get(User.class,"edilov_st@mail.ru");

                    Почему сущность, кроме идентификатора? С таким идентификатором могут быть разные таблицы,
                    поэтому уникальный ключ с помощью которого получаем сущность - это два поля - класс нашей сущности и
                    сам идентификатор.
                    В случае с Get происходит процесс преобразования из РМ в ООМ. А в случае с другими операциями наоборот.
                    А вот тут то и понадобится Hibernate, который будет это делать с помощью Reflection API, конструктор без
                    аргументов.

            */

                session2.getTransaction().commit();
            }

        }

    }
}
