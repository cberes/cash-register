package net.seabears.register.core;

import java.util.List;

public interface DataStore {
    Item getItem(int id);
    List<Item> getItems();
    void createPayment(Payment payment);
    Order createOrder();
    Order getOrder(String id);
    void updateOrder(String id, Order order);
    int incrementAndGetOrderNumber();
}
