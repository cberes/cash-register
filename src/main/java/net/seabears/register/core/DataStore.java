package net.seabears.register.core;

import java.util.List;

/**
 * Operations on the data store.
 * It's expected that these operations will persist data to some sort of database.
 * (Of course, accessors <em>should not</em> modify the database.)
 */
public interface DataStore {
    /** Returns the item with the specified ID (or null if no item with the ID) */
    Item getItem(int id);

    /** Returns a list of all available items */
    List<Item> getItems();

    /** Creates the specified payment */
    void createPayment(Payment payment);

    /** Returns the total amount of payments for the order with the specified ID */
    int getTotalPaid(String id);

    /** Creates and returns the order with specified configuration */
    Order createOrder(OrderConfig orderConfig);

    /** Returns the order with the specified ID (or null if no order with the ID) */
    Order getOrder(String id);

    /** Updates the order with the specified ID to match the specified order */
    void updateOrder(String id, Order order);

    /** Increments the stored order number and returns its value */
    int incrementAndGetOrderNumber();
}
