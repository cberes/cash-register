package net.seabears.register.core;

import java.util.function.Function;

/**
 * An order for a customer that contains varying quantities of items.
 * Methods in this interface should not directly persist changes to the data store.
 */
public interface Order {
    /** Returns the ID to refer to this item. */
    String getId();

    /** Returns whether the order is submitted to the kitchen for preparation. */
    boolean isSubmitted();

    /**
     * Sets the order number by which the customer can identify the order. This marks the order as submitted.
     * @see #isSubmitted()
     */
    void setNumber(int number);

    /** Returns the order's price with subtotal and tax separate */
    OrderTotal getTotal();

    /** Removes the item with the specified ID from the order */
    void removeItem(int id);

    /** Returns whether this order contains the item with the specified ID */
    boolean containsItem(int id);

    /** Adds the item with the specified ID to the order in the specified quantity */
    void addItem(Item item, int quantity);

    /**
     * Updates the item's quantity (or adds it to the order)
     * @param id item's ID
     * @param quantity new quantity
     * @param itemFunc used to the get the item (by ID) if it's not on the order
     */
    void updateItem(int id, int quantity, Function<Integer, Item> itemFunc);
}
