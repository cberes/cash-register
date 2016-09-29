package net.seabears.register.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.seabears.register.core.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class TenderControllerTest {
    @MockBean
    private DataStore data;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    private static final String ORDER_ID = "1";

    private static Payment pay(int amount) {
        Payment payment = new Payment();
        payment.amount = amount;
        payment.method = "CASH";
        payment.orderId = ORDER_ID;
        return payment;
    }

    private static OrderTotal orderTotal(int subtotal, int tax) {
        final OrderTotal total = new OrderTotal();
        total.subtotal = subtotal;
        total.tax = tax;
        return total;
    }

    private Order order(int subtotal, int tax) {
        final Order order = mock(Order.class);
        when(order.getId()).thenReturn(ORDER_ID);
        when(order.getTotal()).thenReturn(orderTotal(subtotal, tax));
        given(data.getOrder(ORDER_ID)).willReturn(order);
        return order;
    }

    private ResultActions makeRequest(Payment payment) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.post("/tender")
                .content(mapper.writeValueAsBytes(payment))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void pay() throws Exception {
        order(100, 10);
        makeRequest(pay(110)).andExpect(status().isOk());
        verify(data).createPayment(any(Payment.class));
    }

    @Test
    public void payOrderNotFound() throws Exception {
        given(data.getOrder(ORDER_ID)).willReturn(null);
        makeRequest(pay(100)).andExpect(status().is4xxClientError());
        verify(data, never()).createPayment(any(Payment.class));
    }

    @Test
    public void payInsufficient() throws Exception {
        order(100, 10);
        makeRequest(pay(109)).andExpect(status().is4xxClientError());
        verify(data, never()).createPayment(any(Payment.class));
    }

    @Test
    public void payZero() throws Exception {
        makeRequest(pay(0)).andExpect(status().is4xxClientError());
        verify(data, never()).createPayment(any(Payment.class));
    }
}

