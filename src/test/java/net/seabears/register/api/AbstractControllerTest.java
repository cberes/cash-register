package net.seabears.register.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.seabears.register.core.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;

import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
abstract class AbstractControllerTest {
    protected final int ITEM_ID;
    protected final String ORDER_ID;

    AbstractControllerTest() {
        this(0, "");
    }

    AbstractControllerTest(String orderId) {
        this(0, orderId);
    }

    AbstractControllerTest(int itemId, String orderId) {
        this.ITEM_ID = itemId;
        this.ORDER_ID = orderId;
    }

    @MockBean
    protected DataStore data;

    @Autowired
    protected MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    protected String toJson(Object o) throws IOException {
        return mapper.writeValueAsString(o);
    }

    protected Item item(String name, int price) {
        Item item = Items.item(ITEM_ID, name, price);
        given(data.getItem(ITEM_ID)).willReturn(item);
        return item;
    }

    protected Order order(int subtotal, int tax) {
        final Order order = Orders.order(ORDER_ID, Orders.total(subtotal, tax));
        given(data.getOrder(ORDER_ID)).willReturn(order);
        return order;
    }
}
