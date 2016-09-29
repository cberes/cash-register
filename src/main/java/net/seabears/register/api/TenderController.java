package net.seabears.register.api;

import net.seabears.register.api.errors.InvalidPaymentException;
import net.seabears.register.api.errors.OrderNotFoundException;
import net.seabears.register.core.DataStore;
import net.seabears.register.core.Order;
import net.seabears.register.core.OrderTotal;
import net.seabears.register.core.Payment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tender")
public class TenderController {
    @Autowired
    private DataStore data;

    @RequestMapping(value = "", method = RequestMethod.POST)
    public void pay(@RequestBody Payment payment) {
        exitIfInvalidPaymentAmount(payment.amount);
        final Order order = getOrderOrExit(payment.orderId);
        exitIfInsufficientPaymentAmount(order.getTotal(), payment.amount);
        data.createPayment(payment);
    }

    private static void exitIfInvalidPaymentAmount(int amount) {
        if (amount < 1) {
            throw new InvalidPaymentException("amount was " + amount + " but must be > 0");
        }
    }

    private Order getOrderOrExit(String id) {
        final Order order = data.getOrder(id);
        if (order == null) {
            throw new OrderNotFoundException("order " + id + " not found");
        }
        return order;
    }

    private static void exitIfInsufficientPaymentAmount(OrderTotal total, int amount) {
        final int totalWithTax = total.subtotal + total.tax;
        if (amount < totalWithTax) {
            throw new InvalidPaymentException("amount was " + amount + " but must be >= " + totalWithTax);
        }
    }
}

