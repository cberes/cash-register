package net.seabears.register.core;

/** An item that can be added to an order (e.g. a specific type of pizza or a brand of drink). */
public interface Item {
    /** Returns the ID to refer to this item. */
    int getId();

    /** Returns the human readable name of the order. */
    String getName();

    /** Returns the item's price in cents. */
    int getPrice();
}
