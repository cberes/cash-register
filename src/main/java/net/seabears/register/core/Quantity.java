package net.seabears.register.core;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Item quantity */
public class Quantity {
    /** Item's ID */
    @JsonProperty("item_id")
    public int itemId;
    /** Amount of the item for an order */
    public int amount;
}
