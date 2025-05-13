package org.broker.classes;

public class Order {
    public enum Side { BUY, SELL }
    public enum OrderType { MARKET, LIMIT, MARKET_ON_OPEN, LIMIT_ON_OPEN}
    public enum TimeInForce { MARKET_ON_OPEN, MARKET_ON_CLOSE, CURRENT_SESSION, GOOD_TILL_CANCELED}


//    String type; // buy, sell
    private Side side; // Направление сделки
    private OrderType orderType;
    private TimeInForce timeInForce;
    private String ticker;
    private int quantity;
    private double limitPrice;

    public Order(String ticker, int quantity, Side side, OrderType orderType, double limitPrice, TimeInForce timeInForce) {
        this.ticker = ticker;
        this.quantity = quantity;
        this.side = side;
        this.orderType = orderType;
        this.limitPrice = limitPrice;
        this.timeInForce = timeInForce;
    }

    @Override
    public String toString() {
        return String.format("Order[%s %d %s @ %s (%s)]",
                side, quantity, ticker,
                orderType == OrderType.LIMIT || orderType == OrderType.LIMIT_ON_OPEN ? limitPrice : timeInForce);
    }

    public Side getSide() {
        return side;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public TimeInForce getTimeInForce() {
        return timeInForce;
    }

    public String getTicker() {
        return ticker;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getLimitPrice() {
        return limitPrice;
    }

}
