package net.seabears.register.core;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Payment details */
public class Payment {
    /** Payment amount in cents */
    public int amount;
    /** Order ID to which this payment applies */
    @JsonProperty("order_id")
    public String orderId;
    /** Payment method (e.g. CASH or CREDIT) */
    public String method;
}
