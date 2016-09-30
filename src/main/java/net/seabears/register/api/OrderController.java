package net.seabears.register.api;

import net.seabears.register.api.errors.*;
import net.seabears.register.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static java.util.Collections.singletonMap;

/** Operations to create and modify orders */
@RestController
@RequestMapping("/orders")
public class OrderController {
    @Autowired
    private DataStore data;

    /**
     * Creates a new order in the system
     * @param orderConfig order details (tax %)
     * @return order ID
     * @throws InvalidTaxException if tax % is negative
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    public Map<String, String> order(@RequestBody OrderConfig orderConfig) {
        exitIfInvalidTax(orderConfig.tax);
        final String id = data.createOrder(orderConfig).getId();
        return singletonMap("id", id);
    }

    private static void exitIfInvalidTax(double tax) {
        if (tax < 0.0) {
            throw new InvalidTaxException("tax was " + tax + " but must be >= 0");
        }
    }

    /**
     * Adds an item to an existing order with quantity 1.
     * Order must not be submitted.
     * The item must not exist on the order.
     * Use {@link #updateItem(String, Quantity)} to change item quantities.
     * @param id order ID
     * @param quantity item ID (if amount is specified, it will be ignored)
     * @return order total
     * @see #updateItem(String, Quantity)
     * @throws DuplicateItemException if the item was added to the order already
     * @throws OrderNotFoundException if the order does not exist
     * @throws SubmittedOrderModificationException if the order is submitted
     * @throws ItemNotFoundException if the item does not exist
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.POST)
    public OrderTotal addItem(@PathVariable String id, @RequestBody Quantity quantity) {
        final Order order = getFreshOrderOrExit(id);
        exitIfOrderContainsItem(order, quantity.itemId);
        final Item item = getItemOrExit(quantity.itemId);
        order.addItem(item, 1);
        data.updateOrder(id, order);
        return order.getTotal();
    }

    /** Returns the order. Errors if order is not found or if it was submitted already. */
    private Order getFreshOrderOrExit(String id) {
        final Order order = data.getOrder(id);
        if (order == null) {
            throw new OrderNotFoundException("order " + id + " not found");
        }
        if (order.isSubmitted()) {
            throw new SubmittedOrderModificationException("order " + id + " was already submitted");
        }
        return order;
    }

    private static void exitIfOrderContainsItem(Order order, int id) {
        if (order.containsItem(id)) {
            throw new DuplicateItemException("item " + id + " is already on the order");
        }
    }

    private Item getItemOrExit(int id) {
        final Item item = data.getItem(id);
        if (item == null) {
            throw new ItemNotFoundException("item " + id + " not found");
        }
        return item;
    }

    /**
     * Updates an item on (or adds an item to) an order with a specified quantity.
     * Order must not be submitted.
     * The quantity must be greater than zero.
     * Use {@link #deleteItem(String, Quantity)} to remove items.
     * @param id order ID
     * @param quantity item ID and amount
     * @return order total
     * @see #deleteItem(String, Quantity)
     * @throws InvalidQuantityException if quantity is zero or negative
     * @throws OrderNotFoundException if the order does not exist
     * @throws SubmittedOrderModificationException if the order is submitted
     * @throws ItemNotFoundException if the item does not exist
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public OrderTotal updateItem(@PathVariable String id, @RequestBody Quantity quantity) {
        exitIfInvalidQuantity(quantity.amount);
        final Order order = getFreshOrderOrExit(id);
        order.updateItem(quantity.itemId, quantity.amount, this::getItemOrExit);
        data.updateOrder(id, order);
        return order.getTotal();
    }

    private static void exitIfInvalidQuantity(int quantity) {
        if (quantity < 1) {
            throw new InvalidQuantityException("quantity was " + quantity + " but must be > 0");
        }
    }

    /**
     * Deletes an item from an order. Order must not be submitted.
     * If the item does not exist, or the item is not on the order, then no changes are made.
     * @param id order ID
     * @param quantity item ID (if amount is specified, it will be ignored)
     * @return order total
     * @throws OrderNotFoundException if the order does not exist
     * @throws SubmittedOrderModificationException if the order is submitted
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public OrderTotal deleteItem(@PathVariable String id, @RequestBody Quantity quantity) {
        final Order order = getFreshOrderOrExit(id);
        order.removeItem(quantity.itemId);
        data.updateOrder(id, order);
        return order.getTotal();
    }

    /**
     * Submits an order. This assigns an "order number" to the order.
     * The customer can identify their order with this number.
     * This means the order is ready for preparations by the kitchen. Once an order is submitted, it cannot be modified.
     * The order must have had items added to it.
     * @param id order ID
     * @return order number
     * @throws OrderNotFoundException if the order does not exist
     * @throws SubmittedOrderModificationException if the order is submitted
     * @throws EmptyOrderException if the order contains no items
     */
    @RequestMapping(value = "/{id}/submit", method = RequestMethod.POST)
    public Map<String, Integer> submit(@PathVariable String id) {
        final Order order = getFreshOrderOrExit(id);
        exitIfOrderIsEmpty(order);
        final int number = data.incrementAndGetOrderNumber();
        order.setNumber(number);
        data.updateOrder(id, order);
        return singletonMap("number", number);
    }

    private static void exitIfOrderIsEmpty(Order order) {
        if (order.isEmpty()) {
            throw new EmptyOrderException("order " + order.getId() + " contains no items");
        }
    }
}
