<?php

enum OrderType {
    case GTC; // Good Till Cancelled
    case IOC; // Immediate Or Cancel
    case FOK; // Fill Or Kill
    case GTD; // Good Till Date
    case OPG; // At the opening
    case CLS; // At the close
}

enum Side {
    case BUY;
    case SELL;
}

class Order {

    private string $ticker;
    private Side $side;
    private OrderType $orderType;
    private int $quantity;
    private float $price;
    private string $userId;

    public function __construct(string $ticker, Side $side, OrderType $orderType, int $quantity, float $price, string $userId) {
        $this->ticker = $ticker;
        $this->side = $side;
        $this->orderType = $orderType;
        $this->quantity = $quantity;
        $this->price = $price;
        $this->userId = $userId;
    }

    public function getTicker(): string {
        return $this->ticker;
    }

    public function getSide(): Side {
        return $this->side;
    }

    public function getOrderType(): OrderType {
        return $this->orderType;
    }

    public function getQuantity(): int {
        return $this->quantity;
    }

    public function getPrice(): float {
        return $this->price;
    }

    public function getUserId(): string {
        return $this->userId;
    }


    public function setQuantity(int $quantity): void {
        $this->quantity = $quantity;
    }

    public function setPrice(float $price): void {
        $this->price = $price;
    }

    public function __toString(): string {
        return "Order for ticker: {$this->ticker}, side: {$this->side->name}, type {$this->orderType->name}, quantity {$this->quantity}, price: {$this->price}, user id: {$this->userId}";
    }
}
