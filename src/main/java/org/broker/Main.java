package org.broker;

import org.broker.classes.Broker;
//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {

        // Создаем объект Broker
        Broker broker = new Broker();
        String clientId = broker.generateUniqueString();
        // Инициализируем клиента
        broker.initializeClient(clientId);

        // Получаем баланс клиента
        double balance = broker.getClientBalance(clientId);
        System.out.println("Баланс клиента " + clientId + " : " + balance);

        // Обновляем баланс клиента
        broker.updateClientBalance(clientId, 9000.0);

        // Получаем количество акций по тикеру
        int stockQuantity = broker.getClientStockQuantity(clientId, "AAPL");
        System.out.println("Количество акций AAPL у клиента " + clientId + " : " + stockQuantity);

        // Обновляем количество акций
        broker.updateClientStockQuantity(clientId, "AAPL", 75);
    }
}