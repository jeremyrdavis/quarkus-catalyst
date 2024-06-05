package io.arrogantprogrammer;

public class Order {

    private int orderId;

    protected Order() {
    }

    protected int getOrderId() {
        return orderId;
    }

    protected void setOrderId(int orderId) {
        this.orderId = orderId;
    }
}
