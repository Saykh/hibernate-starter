package com.dmdev.util;

import com.dmdev.converter.BirthdayConverter;
import com.dmdev.entity.User;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.experimental.UtilityClass;
import org.hibernate.SessionFactory;
import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy;
import org.hibernate.cfg.Configuration;

@UtilityClass
public class HibernateUtil {

    public static SessionFactory buildSessionFactory() {



        /*

            В Configuration (класс) у нас всё, что нужно для создания SF, например, стратегии именования,
            преобразования названий классов в Java в соответствующие названия таблиц, колонок в БД и т.д.
            Кроме того, там есть метаинформация -> информация о наших таблицах.
            Вернет нам SessionFactory на основании всех полей в классе Configuration и плюс
            hibernate.cfg.xml.
            SessionFactory (интерфейс), как и ConnectionPool должен быть один в приложении, и мы должны его закрывать.
            Мы просто инициализируем SessionFactory и из неё получаем Session и оперировать Entity.
            Session (интерфейс) предоставляет функционал, необходимый для Hibernate и управления его сущностями. Он
            отслеживает полный жизненный цикл наших сущностей.
            Именно объект Session под капотом формирует наш SQL запрос с помощью Reflection API.

            SessionFactory ("аналог" ConnectionPool в JDBC) — это фабрика для объектов Session. Обычно
            создается во время запуска приложения и сохраняется для последующего использования.
            Является потокобезопасным объектом и используется всеми потоками приложения.

            Session ("аналог" Connection в JDBC) - обеспечивает физическое соединение между приложением и БД.
            Основная функция - предлагать DML-операции для экземпляров сущностей.

            Точнее это более сложные обёртки в Hibernate, упращающие работу с ORM.

            Query — интерфейс позволяет выполнять запросы к БД. Запросы написаны на HQL или на SQL.

        */


        Configuration configuration = new Configuration();
        configuration.configure();

        /*
           Регистрируем сущность, чтобы Hibernate отслеживал ее. Либо в xml файле (1).
        */
        configuration.addAnnotatedClass(User.class);

        /*
            Для birthDate (из класса) -> birth_date (в БД). Ну или с помощью аннотации Column над
            полем в классе (2).
        */
        configuration.setPhysicalNamingStrategy(new CamelCaseToUnderscoresNamingStrategy());

        /*
            Чтоб над полем BirthDay каждый раз не пришлось устанавливать аннотацию, скажем
            Hibernate, чтоб он автоматом проводил наши преобразования для поля BirthDay в сущности.
            Либо используй аннотацию @Converter над классом в классе конверторе (4).
        */
        configuration.addAttributeConverter(new BirthdayConverter(), true);

        /*
            (5)
        */
        configuration.registerTypeOverride(new JsonBinaryType());

        return configuration.buildSessionFactory();
    }
}
