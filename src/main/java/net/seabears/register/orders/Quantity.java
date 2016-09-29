package net.seabears.register.orders;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Quantity {
    @JsonProperty("item_id")
    public long itemId;
    public int amount;
}
