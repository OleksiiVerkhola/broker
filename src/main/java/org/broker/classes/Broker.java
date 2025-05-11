package org.broker.classes;



public class Broker {

    private DatabaseHandler databaseHandler;

    // Конструктор, инициализирующий DatabaseHandler с нужным URL
    public Broker() {
        this.databaseHandler = new DatabaseHandler();
    }

    public void connectToAccount(String clientId) {
        databaseHandler.getBalance("balance_" + clientId);
    }

    public void createNewAccount(String clientId) {
        initializeClient(clientId);
    }

    public void accountExists(String clientId) {

    }

    // Пример использования метода для создания таблиц клиента
    private void initializeClient(String clientId) {
        databaseHandler.initializeClientTables(clientId);
    }

    public boolean submitOrder(Order order) {
        return true;
    }

    // Проверяет можно ли вообще исполнить такой order
    private boolean canExecuteOrder(Order order) {
        return true;
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
}

