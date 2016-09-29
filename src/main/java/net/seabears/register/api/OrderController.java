package net.seabears.register.api;

import net.seabears.register.api.errors.*;
import net.seabears.register.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {
    @Autowired
    private DataStore data;

    @RequestMapping(value = "", method = RequestMethod.POST)
    public String order(@RequestBody Tax tax) {
        exitIfInvalidTax(tax.tax);
        return data.createOrder(tax).getId();
    }

    private static void exitIfInvalidTax(double tax) {
        if (tax < 0.0) {
            throw new InvalidTaxException("tax was " + tax + " but must be >= 0");
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.POST)
    public OrderTotal addItem(@PathVariable String id, @RequestBody Quantity quantity) {
        final Order order = getOrderOrExit(id);
        exitIfOrderContainsItem(order, quantity.itemId);
        final Item item = getItemOrExit(quantity.itemId);
        order.addItem(item, 1);
        data.updateOrder(id, order);
        return order.getTotal();
    }

    private Order getOrderOrExit(String id) {
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

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public OrderTotal updateItem(@PathVariable String id, @RequestBody Quantity quantity) {
        exitIfInvalidQuantity(quantity.amount);
        final Order order = getOrderOrExit(id);
        order.updateItem(quantity.itemId, quantity.amount, this::getItemOrExit);
        data.updateOrder(id, order);
        return order.getTotal();
    }

    private static void exitIfInvalidQuantity(int quantity) {
        if (quantity < 1) {
            throw new InvalidQuantityException("quantity was " + quantity + " but must be > 0");
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public OrderTotal deleteItem(@PathVariable String id, @RequestBody Quantity quantity) {
        final Order order = getOrderOrExit(id);
        order.removeItem(quantity.itemId);
        data.updateOrder(id, order);
        return order.getTotal();
    }

    @RequestMapping(value = "/{id}/submit", method = RequestMethod.POST)
    public int submit(@PathVariable String id) {
        final Order order = getOrderOrExit(id);
        final int number = data.incrementAndGetOrderNumber();
        order.setNumber(number);
        data.updateOrder(id, order);
        return number;
    }
}

