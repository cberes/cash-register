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
     * @throws InvalidPaymentException if payment amount is zero or negative
     * @throws OrderNotFoundException if order does not exist
     */
    @RequestMapping(value = "", method = RequestMethod.POST)
    public void pay(@RequestBody Payment payment) {
        exitIfInvalidPaymentAmount(payment.amount);
        exitIfOrderMissing(payment.orderId);
        data.createPayment(payment);
    }

    private static void exitIfInvalidPaymentAmount(int amount) {
        if (amount < 1) {
            throw new InvalidPaymentException("amount was " + amount + " but must be > 0");
        }
    }

    private void exitIfOrderMissing(String id) {
        if (data.getOrder(id) == null) {
            throw new OrderNotFoundException("order " + id + " not found");
        }
    }
}

