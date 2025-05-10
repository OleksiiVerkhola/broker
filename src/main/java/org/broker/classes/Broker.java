package org.broker.classes;

import java.util.UUID;

public class Broker {

    private DatabaseHandler databaseHandler;

    // Конструктор, инициализирующий DatabaseHandler с нужным URL
    public Broker() {
        this.databaseHandler = new DatabaseHandler();
    }

    // Пример использования метода для создания таблиц клиента
    public void initializeClient(String clientId) {
        databaseHandler.initializeClientTables(clientId);
    }

    // Пример использования метода для получения баланса клиента
    public double getClientBalance(String clientId) {
        String tableId = "balance_" + clientId;
        return databaseHandler.getBalance(tableId);
    }

    // Пример использования метода для обновления баланса клиента
    public void updateClientBalance(String clientId, double newBalance) {
        String tableId = "balance_" + clientId;
        databaseHandler.updateBalance(tableId, newBalance);
    }

    // Пример использования метода для получения количества акций клиента по тикеру
    public int getClientStockQuantity(String clientId, String ticker) {
        String tableId = "portfolio_" + clientId;
        return databaseHandler.getStockQuantity(tableId, ticker);
    }

    // Пример использования метода для обновления количества акций клиента
    public void updateClientStockQuantity(String clientId, String ticker, int quantity) {
        String tableId = "portfolio_" + clientId;
        databaseHandler.updateStockQuantity(tableId, ticker, quantity);
    }

    public String generateUniqueString() {
        // Убираю дефисы из строки потому что sql на них ругается.
        return UUID.randomUUID().toString().replace("-", "");
    }
}

