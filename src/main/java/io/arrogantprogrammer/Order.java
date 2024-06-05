package io.arrogantprogrammer;

public class Order {

    private int orderId;

    protected Order() {
    }

    public int getOrderId() {
        return orderId;
    }

    protected void setOrderId(int orderId) {
        this.orderId = orderId;
    }
}
