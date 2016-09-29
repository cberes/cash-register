package net.seabears.register.api;

import net.seabears.register.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {
    @Autowired
    private DataStore data;

    @RequestMapping(value = "", method = RequestMethod.PUT)
    public String newOrder() {
        return data.createOrder().getId();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.POST)
    public int addItem(@PathVariable String id, @RequestBody Quantity quantity) {
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
            throw new NotFoundException("order " + id + " not found");
        }
        if (order.isSubmitted()) {
            throw new BadRequestException("cannot modify a submitted order");
        }
        return order;
    }

    private void exitIfOrderContainsItem(Order order, int id) {
        if (order.containsItem(id)) {
            throw new BadRequestException("item " + id + " is already on the order");
        }
    }

    private Item getItemOrExit(int id) {
        final Item item = data.getItem(id);
        if (item == null) {
            throw new NotFoundException("item " + id + " not found");
        }
        return item;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public int updateItem(@PathVariable String id, @RequestBody Quantity quantity) {
        exitIfInvalidQuantity(quantity.amount);
        final Order order = getOrderOrExit(id);
        order.updateItem(quantity.itemId, quantity.amount, this::getItemOrExit);
        data.updateOrder(id, order);
        return order.getTotal();
    }

    private void exitIfInvalidQuantity(int quantity) {
        if (quantity < 1) {
            throw new BadRequestException("quantity was " + quantity + " but must be > 0");
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public int deleteItem(@PathVariable String id, @RequestBody Quantity quantity) {
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

