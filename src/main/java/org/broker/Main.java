package org.broker;

import org.broker.classes.Broker;
import org.broker.classes.Order;
import org.broker.classes.Security;

import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
//
//        // Создаем объект Broker
//        Broker broker = new Broker();
//        String clientId = broker.generateUniqueString();
//        // Инициализируем клиента
//        broker.initializeClient(clientId);
//
//        // Получаем баланс клиента
//        double balance = broker.getClientBalance(clientId);
//        System.out.println("Баланс клиента " + clientId + " : " + balance);
//
//        // Обновляем баланс клиента
//        broker.updateClientBalance(clientId, 9000.0);
//
//        // Получаем количество акций по тикеру
//        int stockQuantity = broker.getClientStockQuantity(clientId, "AAPL");
//        System.out.println("Количество акций AAPL у клиента " + clientId + " : " + stockQuantity);
//
//        // Обновляем количество акций
//        broker.updateClientStockQuantity(clientId, "AAPL", 75);

        Order order = new Order("AAPL", 10, Order.Side.BUY, Order.OrderType.LIMIT, 10.0, Order.TimeInForce.MARKET_ON_OPEN);

        // f9b5ba388b974e9cb83713c03e2ae9f1
        Broker broker = new Broker("f9b5ba388b974e9cb83713c03e2ae9f5", LocalDate.of(1991, 1, 1));
//        broker.updateClientBalance(100000);
//        broker.switchToNextSession();
//
//        broker.placeOrder(order);
    }
}