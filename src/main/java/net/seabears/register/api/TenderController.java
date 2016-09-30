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

import java.util.Map;

import static java.util.Collections.singletonMap;
import static net.seabears.register.api.OrderController.exitIfOrderIsEmpty;

/** Operations such as paying for orders */
@RestController
@RequestMapping("/tender")
public class TenderController {
    @Autowired
    private DataStore data;

    /**
     * Pay for an order. Can be a split tender, in which case the payment amount will not meet the order's total, and
     * another request to the endpoint will be expected.
     * @param payment payment information (amount, method, order ID)
     * @return remaining balance
     * @throws InvalidPaymentException if payment amount is zero or negative
     * @throws OrderNotFoundException if order does not exist
     * @throws net.seabears.register.api.errors.EmptyOrderException if order is empty
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    public Map<String, Integer> pay(@RequestBody Payment payment) {
        exitIfInvalidPaymentAmount(payment.amount);
        Order order = getOrderOrExit(payment.orderId);
        exitIfOrderIsEmpty(order);
        // get the balance BEFORE writing the payment to the database
        // then we know it is not included in the total paid returned by the DataStore
        int balance = getRemainingBalance(order) - payment.amount;
        data.createPayment(payment);
        return singletonMap("remaining", balance);
    }

    private static void exitIfInvalidPaymentAmount(int amount) {
        if (amount < 1) {
            throw new InvalidPaymentException("amount was " + amount + " but must be > 0");
        }
    }

    private Order getOrderOrExit(String id) {
        Order order = data.getOrder(id);
        if (order == null) {
            throw new OrderNotFoundException("order " + id + " not found");
        }
        return order;
    }

    private int getRemainingBalance(Order order) {
        OrderTotal total = order.getTotal();
        return total.subtotal + total.tax - data.getTotalPaid(order.getId());
    }
}

