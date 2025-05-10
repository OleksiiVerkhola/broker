package org.broker.classes;

import java.sql.*;

public class DatabaseInitializer {

    private static final String DB_URL = "jdbc:sqlite:trading.db";

    public static void initializeClientTables(int clientId) {
        String portfolioTable = "portfolio_" + clientId;
        String balanceTable = "balance_" + clientId;

        String createPortfolioTableSQL = String.format("""
            CREATE TABLE IF NOT EXISTS %s (
                ticker TEXT PRIMARY KEY,
                quantity INTEGER NOT NULL
            );
        """, portfolioTable);

        String createBalanceTableSQL = String.format("""
            CREATE TABLE IF NOT EXISTS %s (
                id INTEGER PRIMARY KEY CHECK (id = 1),
                balance REAL NOT NULL
            );
        """, balanceTable);

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            stmt.execute(createPortfolioTableSQL);
            stmt.execute(createBalanceTableSQL);

            String insertInitialBalanceSQL = String.format("""
                INSERT OR IGNORE INTO %s (id, balance) VALUES (1, 0.0);
            """, balanceTable);
            stmt.execute(insertInitialBalanceSQL);

            System.out.println("Таблицы для клиента " + clientId + " успешно созданы.");

        } catch (SQLException e) {
            System.err.println("Ошибка при инициализации БД: " + e.getMessage());
        }
    }

    public static double getBalance(String balanceTable) {
        String sql = "SELECT balance FROM " + balanceTable + " WHERE id = 1";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getDouble("balance");
            }
        } catch (SQLException e) {
            System.err.println("Ошибка при получении баланса: " + e.getMessage());
        }
        return 0.0;
    }

    public static int getStockQuantity(String portfolioTable, String ticker) {
        String sql = "SELECT quantity FROM " + portfolioTable + " WHERE ticker = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

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

    public static void updateBalance(String balanceTable, double amount) {
        String selectSql = "SELECT balance FROM " + balanceTable + " WHERE id = 1";
        String updateSql = "UPDATE " + balanceTable + " SET balance = ? WHERE id = 1";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement selectStmt = conn.createStatement();
             ResultSet rs = selectStmt.executeQuery(selectSql)) {

            if (rs.next()) {
                double currentBalance = rs.getDouble("balance");
                double newBalance = currentBalance + amount;

                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setDouble(1, newBalance);
                    updateStmt.executeUpdate();
                }
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при обновлении баланса: " + e.getMessage());
        }
    }

    public static void updateStockQuantity(String portfolioTable, String ticker, int quantity) {
        // String selectSql = "SELECT quantity FROM " + portfolioTable + " WHERE ticker = ?";
        String insertSql = "INSERT INTO " + portfolioTable + " (ticker, quantity) VALUES (?, ?)";
        String updateSql = "UPDATE " + portfolioTable + " SET quantity = ? WHERE ticker = ?";
        String deleteSql = "DELETE FROM " + portfolioTable + " WHERE ticker = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            int current = getStockQuantity(portfolioTable, ticker);

            if (quantity <= 0) {
                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                    deleteStmt.setString(1, ticker);
                    deleteStmt.executeUpdate();
                }
            } else if (current == 0) {
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setString(1, ticker);
                    insertStmt.setInt(2, quantity);
                    insertStmt.executeUpdate();
                }
            } else {
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setInt(1, quantity);
                    updateStmt.setString(2, ticker);
                    updateStmt.executeUpdate();
                }
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при обновлении количества акций: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        int clientId = 1;
        initializeClientTables(clientId);

        String portfolioTable = "portfolio_" + clientId;
        String balanceTable = "balance_" + clientId;

        updateBalance(balanceTable, 1000.0);
        System.out.println("Баланс клиента: " + getBalance(balanceTable));

        updateStockQuantity(portfolioTable, "AAPL", 5);
        System.out.println("AAPL: " + getStockQuantity(portfolioTable, "AAPL") + " акций");

        updateStockQuantity(portfolioTable, "AAPL", 0); // удаление тикера
    }
}
