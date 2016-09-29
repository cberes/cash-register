package net.seabears.register.core;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Quantity {
    @JsonProperty("item_id")
    public int itemId;
    public int amount;
}
