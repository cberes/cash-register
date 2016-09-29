package net.seabears.register.core;

import static org.mockito.BDDMockito.*;

public final class Orders {
    private Orders() {
        throw new UnsupportedOperationException("cannot instantiate " + getClass());
    }

    public static OrderConfig config(double tax) {
        OrderConfig order = new OrderConfig();
        order.tax = tax;
        return order;
    }

    public static OrderTotal total(int subtotal, int tax) {
        final OrderTotal total = new OrderTotal();
        total.subtotal = subtotal;
        total.tax = tax;
        return total;
    }

    public static Order order(String id, OrderTotal total) {
        final Order order = mock(Order.class);
        when(order.getId()).thenReturn(id);
        when(order.getTotal()).thenReturn(total);
        return order;
    }
}
