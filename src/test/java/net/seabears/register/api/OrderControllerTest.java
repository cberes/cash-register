package net.seabears.register.api;

import net.seabears.register.core.*;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.function.Function;

import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class OrderControllerTest extends AbstractControllerTest {
    public OrderControllerTest() {
        super(1, "1");
    }

    private ResultActions request(HttpMethod method, String uri) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.request(method, uri)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON));
    }

    private ResultActions request(HttpMethod method, String uri, Object content) throws Exception {
        return mvc.perform(MockMvcRequestBuilders.request(method, uri)
                .content(toJson(content))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void order() throws Exception {
        final double expectedTax = 0.09;
        final Order order = order(0, 0);
        given(data.createOrder(any(OrderConfig.class))).willReturn(order);
        request(HttpMethod.POST, "/orders", Orders.config(expectedTax))
                .andExpect(status().isOk())
                .andExpect(content().json(toJson(singletonMap("id", ORDER_ID))));
        ArgumentCaptor<OrderConfig> configArgumentCaptor = ArgumentCaptor.forClass(OrderConfig.class);
        verify(data).createOrder(configArgumentCaptor.capture());
        assertEquals(expectedTax, configArgumentCaptor.getValue().tax, 1E-9);
    }

    @Test
    public void orderInvalidTax() throws Exception {
        OrderConfig config = Orders.config(-0.01);
        request(HttpMethod.POST, "/orders", config).andExpect(status().is4xxClientError());
        verify(data, never()).createOrder(any(OrderConfig.class));
    }

    @Test
    public void submit() throws Exception {
        final int expectedNumber = 1;
        Order order = order(0, 0);
        given(order.isSubmitted()).willReturn(false);
        given(data.incrementAndGetOrderNumber()).willReturn(expectedNumber);
        request(HttpMethod.POST, "/orders/" + ORDER_ID + "/submit")
                .andExpect(status().isOk())
                .andExpect(content().json(toJson(singletonMap("number", expectedNumber))));
        verify(order).setNumber(expectedNumber);
        verify(data).updateOrder(eq(ORDER_ID), same(order));
    }

    @Test
    public void submitSubmittedOrder() throws Exception {
        Order order = order(0, 0);
        given(order.isSubmitted()).willReturn(true);
        request(HttpMethod.POST, "/orders/" + ORDER_ID + "/submit")
                .andExpect(status().is4xxClientError());
        verify(data, never()).updateOrder(anyString(), any(Order.class));
    }

    @Test
    public void submitMissingOrder() throws Exception {
        given(data.getOrder(ORDER_ID)).willReturn(null);
        request(HttpMethod.POST, "/orders/" + ORDER_ID + "/submit")
                .andExpect(status().is4xxClientError());
        verify(data, never()).updateOrder(anyString(), any(Order.class));
    }

    @Test
    public void addItem() throws Exception {
        Order order = order(90, 10);
        Item item = item("pizza", 90);
        given(order.containsItem(item.getId())).willReturn(false);
        request(HttpMethod.POST, "/orders/" + ORDER_ID, Items.quantity(item.getId()))
                .andExpect(status().isOk())
                .andExpect(content().json(toJson(Orders.total(90, 10))));
        verify(order).addItem(item, 1);
        verify(data).updateOrder(eq(ORDER_ID), same(order));
    }

    @Test
    public void addItemToMissingOrder() throws Exception {
        given(data.getOrder(ORDER_ID)).willReturn(null);
        request(HttpMethod.POST, "/orders/" + ORDER_ID, Items.quantity(ITEM_ID))
                .andExpect(status().is4xxClientError());
        verify(data, never()).updateOrder(anyString(), any(Order.class));
    }

    @Test
    public void addItemToSubmittedOrder() throws Exception {
        Order order = order(0, 0);
        given(order.isSubmitted()).willReturn(true);
        request(HttpMethod.POST, "/orders/" + ORDER_ID, Items.quantity(ITEM_ID))
                .andExpect(status().is4xxClientError());
        verify(data, never()).updateOrder(eq(ORDER_ID), same(order));
    }

    @Test
    public void addDuplicateItem() throws Exception {
        Order order = order(0, 0);
        Item item = item("pizza", 0);
        when(order.containsItem(item.getId())).thenReturn(true);
        request(HttpMethod.POST, "/orders/" + ORDER_ID, Items.quantity(item.getId()))
                .andExpect(status().is4xxClientError());
        verify(data, never()).updateOrder(eq(ORDER_ID), same(order));
    }

    @Test
    public void addMissingItem() throws Exception {
        Order order = order(0, 0);
        given(data.getItem(ITEM_ID)).willReturn(null);
        request(HttpMethod.POST, "/orders/" + ORDER_ID, Items.quantity(ITEM_ID))
                .andExpect(status().is4xxClientError());
        verify(data, never()).updateOrder(eq(ORDER_ID), same(order));
    }

    @Test
    public void updateItem() throws Exception {
        final int amount = 2;
        Order order = order(90, 10);
        Item item = item("pizza", 90);
        request(HttpMethod.PUT, "/orders/" + ORDER_ID, Items.quantity(item.getId(), amount))
                .andExpect(status().isOk())
                .andExpect(content().json(toJson(Orders.total(90, 10))));
        verify(order).updateItem(eq(item.getId()), eq(amount), any(Function.class));
        verify(data).updateOrder(eq(ORDER_ID), same(order));
    }

    @Test
    public void updateItemWithInvalidQuantity() throws Exception {
        request(HttpMethod.PUT, "/orders/" + ORDER_ID, Items.quantity(ITEM_ID, 0))
                .andExpect(status().is4xxClientError());
        verify(data, never()).updateOrder(anyString(), any(Order.class));
    }

    @Test
    public void updateItemToMissingOrder() throws Exception {
        given(data.getOrder(ORDER_ID)).willReturn(null);
        request(HttpMethod.PUT, "/orders/" + ORDER_ID, Items.quantity(ITEM_ID, 1))
                .andExpect(status().is4xxClientError());
        verify(data, never()).updateOrder(anyString(), any(Order.class));
    }

    @Test
    public void updateItemToSubmittedOrder() throws Exception {
        Order order = order(0, 0);
        given(order.isSubmitted()).willReturn(true);
        request(HttpMethod.PUT, "/orders/" + ORDER_ID, Items.quantity(ITEM_ID, 1))
                .andExpect(status().is4xxClientError());
        verify(data, never()).updateOrder(eq(ORDER_ID), same(order));
    }

    @Test
    public void updateMissingItem() throws Exception {
        Order order = order(0, 0);
        given(data.getItem(ITEM_ID)).willReturn(null);
        request(HttpMethod.PUT, "/orders/" + ORDER_ID, Items.quantity(ITEM_ID))
                .andExpect(status().is4xxClientError());
        verify(data, never()).updateOrder(eq(ORDER_ID), same(order));
    }

    @Test
    public void deleteItem() throws Exception {
        Order order = order(90, 10);
        request(HttpMethod.DELETE, "/orders/" + ORDER_ID, Items.quantity(ITEM_ID))
                .andExpect(status().isOk())
                .andExpect(content().json(toJson(Orders.total(90, 10))));
        verify(order).removeItem(ITEM_ID);
        verify(data).updateOrder(eq(ORDER_ID), same(order));
    }

    @Test
    public void deleteItemFromMissingOrder() throws Exception {
        given(data.getOrder(ORDER_ID)).willReturn(null);
        request(HttpMethod.DELETE, "/orders/" + ORDER_ID, Items.quantity(ITEM_ID))
                .andExpect(status().is4xxClientError());
        verify(data, never()).updateOrder(anyString(), any(Order.class));
    }

    @Test
    public void deleteItemFromSubmittedOrder() throws Exception {
        Order order = order(0, 0);
        given(order.isSubmitted()).willReturn(true);
        request(HttpMethod.DELETE, "/orders/" + ORDER_ID, Items.quantity(ITEM_ID))
                .andExpect(status().is4xxClientError());
        verify(data, never()).updateOrder(eq(ORDER_ID), same(order));
    }
}
