package net.seabears.register.api;

import net.seabears.register.core.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.IOException;

import static java.util.Collections.singletonMap;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class TenderControllerTest extends AbstractControllerTest {
    public TenderControllerTest() {
        super("1");
    }

    private Payment pay(int amount) {
        Payment payment = new Payment();
        payment.amount = amount;
        payment.method = "CASH";
        payment.orderId = ORDER_ID;
        return payment;
    }

    private ResultActions makeRequest(Payment payment) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.post("/tender")
                .content(toJson(payment))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON));
    }

    private String response(int remaining) throws IOException {
        return toJson(singletonMap("remaining", remaining));
    }

    @Test
    public void pay() throws Exception {
        Order order = order(100, 10);
        given(order.isEmpty()).willReturn(false);
        given(data.getTotalPaid("order_" + ORDER_ID)).willReturn(0);
        makeRequest(pay(110))
                .andExpect(status().isOk())
                .andExpect(content().json(response(0)));
        verify(data).createPayment(any(Payment.class));
    }

    @Test
    public void paySplitWithChangeDue() throws Exception {
        Order order = order(100, 10);
        given(order.isEmpty()).willReturn(false);
        given(data.getTotalPaid(ORDER_ID)).willReturn(50);
        makeRequest(pay(75))
                .andExpect(status().isOk())
                .andExpect(content().json(response(-15)));
        verify(data).createPayment(any(Payment.class));
    }

    @Test
    public void payEmpty() throws Exception {
        Order order = order(100, 10);
        given(order.isEmpty()).willReturn(true);
        makeRequest(pay(110)).andExpect(status().is4xxClientError());
        verify(data, never()).createPayment(any(Payment.class));
    }

    @Test
    public void payOrderNotFound() throws Exception {
        given(data.getOrder(ORDER_ID)).willReturn(null);
        makeRequest(pay(100)).andExpect(status().is4xxClientError());
        verify(data, never()).createPayment(any(Payment.class));
    }

    @Test
    public void payZero() throws Exception {
        makeRequest(pay(0)).andExpect(status().is4xxClientError());
        verify(data, never()).createPayment(any(Payment.class));
    }
}
