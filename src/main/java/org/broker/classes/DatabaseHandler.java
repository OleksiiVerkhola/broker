package org.broker.classes;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDate;
import java.util.Properties;


public class DatabaseHandler {

    private static String dbUrl;

    // Конструктор для инициализации URL базы данных
    public DatabaseHandler() {

    }

    // Метод для получения соединения с базой данных
    private Connection getConnection() throws SQLException {
        try (InputStream input = DatabaseHandler.class.getClassLoader().getResourceAsStream("config.properties")) {
            Properties prop = new Properties();
            if (input != null) {
                prop.load(input);
                dbUrl = prop.getProperty("db.path");
            } else {
                throw new RuntimeException("config.properties не найден в папке resources");
            }
        } catch (IOException ex) {
            throw new RuntimeException("Ошибка при загрузке config.properties", ex);
        }
        return DriverManager.getConnection(dbUrl);

    }

    // Создание таблиц для клиента
    public void initializeClientTables(String clientId) {
        String portfolioTable = "portfolio_" + clientId;
        String balanceTable = "balance_" + clientId;
        String ordersTable = "orders_" + clientId;

        String createPortfolioTableSQL = String.format("""
            CREATE TABLE IF NOT EXISTS %s (
                ticker TEXT PRIMARY KEY,
                quantity INTEGER NOT NULL
            );
        """, portfolioTable);

        String createBalanceTableSQL = String.format("""
            CREATE TABLE IF NOT EXISTS %s (
                id INTEGER PRIMARY KEY,
                balance REAL NOT NULL
            );
        """, balanceTable);

        String createOrdersTableSQL = String.format("""
            CREATE TABLE IF NOT EXISTS %s (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            order_date TEXT NOT NULL,
            ticker TEXT NOT NULL,
            side TEXT NOT NULL,           -- BUY / SELL
            order_type TEXT NOT NULL,     -- MARKET, LIMIT, etc.
            time_in_force TEXT NOT NULL,  -- GTC, IOC, etc.
            quantity INTEGER NOT NULL,
            limit_price REAL,             -- может быть NULL для MARKET-ордеров
            status TEXT DEFAULT 'NEW'    -- статус ордера: NEW, EXECUTED, CANCELLED
            );
        """, ordersTable);


        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(createPortfolioTableSQL);
            stmt.execute(createBalanceTableSQL);
            stmt.execute(createOrdersTableSQL);

            String insertInitialBalanceSQL = String.format("""
                INSERT OR IGNORE INTO %s (id, balance) VALUES (1, 0.0);
            """, balanceTable);
            stmt.execute(insertInitialBalanceSQL);

            System.out.println("Таблицы для клиента " + clientId + " успешно созданы.");
        } catch (SQLException e) {
            System.err.println("Ошибка при инициализации БД: " + e.getMessage());
        }
    }

    // Получение баланса для клиента
    public double getBalance(String tableId) {
        String sql = "SELECT balance FROM " + tableId + " WHERE id = 1";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getDouble("balance");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении баланса: " + e.getMessage());
        }
        return 0.0;
    }

    // Обновление баланса клиента
    public void updateBalance(String tableId, double amount) {
        String sql = "UPDATE " + tableId + " SET balance = ? WHERE id = 1";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, amount);
            stmt.executeUpdate();
            System.out.println("Баланс обновлен на: " + amount);
        } catch (SQLException e) {
            System.err.println("Ошибка при обновлении баланса: " + e.getMessage());
        }
    }

    // Получение количества акций для клиента по тикеру
    public int getStockQuantity(String tableId, String ticker) {
        String sql = "SELECT quantity FROM " + tableId + " WHERE ticker = ?";
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, ticker);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("quantity");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении количества акций: " + e.getMessage());
        }
        return 0;
    }

    // Обновление количества акций для клиента
    public void updateStockQuantity(String tableId, String ticker, int quantity) {
        String selectSql = "SELECT COUNT(*) FROM " + tableId + " WHERE ticker = ?";
        String updateSql = "UPDATE " + tableId + " SET quantity = ? WHERE ticker = ?";
        String insertSql = "INSERT INTO " + tableId + " (ticker, quantity) VALUES (?, ?)";

        try (Connection conn = getConnection()) {
            // Проверяем наличие тикера
            boolean exists;
            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                selectStmt.setString(1, ticker);
                ResultSet rs = selectStmt.executeQuery();
                exists = rs.next() && rs.getInt(1) > 0;
            }

            // Обновляем или вставляем
            if (exists) {
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setInt(1, quantity);
                    updateStmt.setString(2, ticker);
                    updateStmt.executeUpdate();
                    System.out.println("Обновлено количество акций для " + ticker + ": " + quantity);
                }
            } else {
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setString(1, ticker);
                    insertStmt.setInt(2, quantity);
                    insertStmt.executeUpdate();
                    System.out.println("Добавлен новый тикер " + ticker + " с количеством: " + quantity);
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при обновлении количества акций: " + e.getMessage());
        }
    }

    public void saveOrder(String tableId, Order order, LocalDate sessionDate) {
        String sql = " INSERT INTO " + tableId + " (order_date, ticker, quantity, side, order_type, time_in_force, limit_price) VALUES (?, ?, ?, ?, ?, ?, ?); ";
        System.out.println(sql);
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, sessionDate.toString());
            stmt.setString(2, order.getTicker());
            stmt.setInt(3, order.getQuantity());
            stmt.setString(4, order.getSide().name());  // Переводим enum в строку
            stmt.setString(5, order.getOrderType().name());  // Переводим enum в строку
            stmt.setString(6, order.getTimeInForce().name());  // Переводим enum в строку
            stmt.setDouble(7, order.getLimitPrice());

            stmt.executeUpdate();  // Выполняем вставку

            System.out.println("Ордер успешно сохранен в таблице");
        } catch (SQLException e) {
            System.err.println("Ошибка при сохранении ордера: " + e.getMessage());
        }
    }

    // Проверка, существует ли таблица в базе данных
    public boolean tableExists(String tableId) {
        String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name=?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tableId);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // true, если таблица найдена

        } catch (SQLException e) {
            System.err.println("Ошибка при проверке существования таблицы: " + e.getMessage());
            return false;
        }
    }


}
