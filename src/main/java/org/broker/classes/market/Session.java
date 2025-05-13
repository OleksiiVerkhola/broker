package org.broker.classes.market;

import java.time.LocalDate;

public class Session {

    private LocalDate date;
    private double open;
    private double high;
    private double low;
    private double close;
    private int volume;

    public Session() {
        this.date = LocalDate.of(1991, 1, 1);
        this.open = 10.0;
        this.high = 12.0;
        this.low = 8.0;
        this.close = 11.0;
    }

    public LocalDate getDate() {
        return date;
    }

    public double getOpen() {
        return open;
    }

    public double getHigh() {
        return high;
    }

    public double getLow() {
        return low;
    }

    public double getClose() {
        return close;
    }

    public int getVolume() {
        return volume;
    }
}
