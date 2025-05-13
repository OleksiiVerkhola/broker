package org.broker.classes;


import org.broker.classes.market.Market;
import org.broker.classes.market.Session;

import java.time.LocalDate;

public class Broker {

    private DatabaseHandler databaseHandler;
    private String clientId;
    private double brokerCommission = 0.39; // %

    public LocalDate getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(LocalDate sessionDate) {
        this.sessionDate = sessionDate;
    }

    private LocalDate sessionDate;

    // Конструктор, инициализирующий DatabaseHandler с нужным URL
    public Broker() {
        this.databaseHandler = new DatabaseHandler();
    }

    public Broker(String clientId) {
        this.databaseHandler = new DatabaseHandler();
        this.clientId = clientId;
        // Дата по умолчанию
        this.sessionDate = LocalDate.of(1991, 1, 1);
        if (!databaseHandler.tableExists("balance_" + this.clientId)) {
            databaseHandler.initializeClientTables(this.clientId);
            System.out.println(clientId + " added to db.");
        } else {
            System.out.println(clientId + " exists.");
        }
    }

    public Broker(String clientId, LocalDate sessionDate) {
        this.databaseHandler = new DatabaseHandler();
        this.clientId = clientId;
        this.sessionDate = sessionDate;
        if (!databaseHandler.tableExists("balance_" + this.clientId)) {
            databaseHandler.initializeClientTables(this.clientId);
            System.out.println(clientId + " added to db.");
        } else {
            System.out.println(clientId + " exists.");
        }
    }

    // Используется для перехода на следующий торговый день и возвращает true
    // если следующий день ещё не наступил в реальном календаре возвращает false
    public boolean switchToNextSession() {
        // Нужно сначала выполнить все ордера на текущую сессию, а затем изменить дату
        this.sessionDate = sessionDate.plusDays(1);
        if (sessionDate.isAfter(LocalDate.now())) {
            return false;
        } else {
            return true;
        }
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

    public boolean placeOrder(Order order) {
        if (canExecuteOrder(order)) {
            // Покупка по цене открытия выполняем сразу, как только получили ордер
            if (order.getTimeInForce() == Order.TimeInForce.MARKET_ON_OPEN && order.getSide() == Order.Side.BUY) {
                // Здесь ещё нужно проверить существует ли такая сессия вообще
                Market market = new Market();
                Session s = market.getSession("AAPL", sessionDate);
                System.out.println(s.getOpen());
                if (s.getOpen() > 0.0) {
                    return executeBuyOrder(order);
                }
            }
        }
        return true;
    }

    // Проверяет можно ли вообще исполнить такой order
    private boolean canExecuteOrder(Order order) {
        return true;
    }

    // Выполняет ордер на покупку
    private boolean executeBuyOrder(Order order) {
        double limitPrice = order.getLimitPrice();
        int quantity = order.getQuantity();
        double totalOrderCost = limitPrice * quantity + (limitPrice * quantity / 100 * brokerCommission);
        System.out.println(totalOrderCost);
        double updatedClientBalance = getClientBalance() - totalOrderCost;
        int updatedClientStockQuantity = getClientStockQuantity(order.getTicker()) + order.getQuantity();

        updateClientBalance(updatedClientBalance);
        updateClientStockQuantity(order.getTicker(), updatedClientStockQuantity);

        return true;
    }

    // Выполняет ордер на продажу
    private boolean executeSellOrder(Order order) {

        return true;
    }

    // Пример использования метода для получения баланса клиента
    public double getClientBalance() {
        String tableId = "balance_" + clientId;
        return databaseHandler.getBalance(tableId);
    }

    // Пример использования метода для обновления баланса клиента
    public void updateClientBalance(double newBalance) {
        String tableId = "balance_" + this.clientId;
        databaseHandler.updateBalance(tableId, newBalance);
    }

    // Пример использования метода для получения количества акций клиента по тикеру
    public int getClientStockQuantity(String ticker) {
        String tableId = "portfolio_" + clientId;
        return databaseHandler.getStockQuantity(tableId, ticker);
    }

    // Пример использования метода для обновления количества акций клиента
    public void updateClientStockQuantity(String ticker, int quantity) {
        String tableId = "portfolio_" + this.clientId;
        databaseHandler.updateStockQuantity(tableId, ticker, quantity);
    }
}

