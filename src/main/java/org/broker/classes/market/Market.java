package org.broker.classes.market;

import java.time.LocalDate;

public class Market {
    public Session getSession(String ticker, LocalDate sessionDate) {
        // Может быть что такой сессии не существует (рынок был выходной), значит нужно возвращать пустой session
        Session session = new Session();
        return session;
    }
}
