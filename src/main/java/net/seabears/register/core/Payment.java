package net.seabears.register.core;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Payment {
    public int amount;
    @JsonProperty("order_id")
    public String orderId;
    public String method;
}