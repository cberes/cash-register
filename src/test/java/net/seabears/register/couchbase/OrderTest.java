package net.seabears.register.couchbase;

import net.seabears.register.core.Items;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class OrderTest {
    @Test
    public void getId() {
        Order order = new Order("foo", 0.0);
        assertThat(order.getId(), equalTo("foo"));
    }

    @Test
    public void isSubmitted() {
        Order order = new Order("foo", 0.0);
        assertThat(order.isSubmitted(), equalTo(false));
        order.setNumber(1);
        assertThat(order.isSubmitted(), equalTo(true));
    }

    @Test
    public void getTotalNoItems() {
        Order order = new Order("foo", 0.0);
        assertThat(order.getTotal().subtotal, equalTo(0));
        assertThat(order.getTotal().tax, equalTo(0));
    }

    @Test
    public void getTotal() {
        Order order = new Order("foo", 0.09);
        order.addItem(Items.item(1, "pizza", 1000), 2);
        order.addItem(Items.item(2, "drink", 200), 4);
        assertThat(order.getTotal().subtotal, equalTo(2800));
        assertThat(order.getTotal().tax, equalTo(252));
    }

    @Test(expected = IllegalArgumentException.class)
    public void addItemWithInvalidQuantity() {
        Order order = new Order("foo", 0.09);
        order.addItem(Items.item(1, "pizza", 1000), 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void taxInvalid() {
        new Order("foo", -0.01);
    }

    @Test(expected = NullPointerException.class)
    public void addNullItem() {
        Order order = new Order("foo", 0.09);
        order.addItem(null, 1);
    }

    @Test
    public void containsItem() {
        Order order = new Order("foo", 0.09);
        assertThat(order.containsItem(2), equalTo(false));
        order.addItem(Items.item(1, "pizza", 1000), 1);
        assertThat(order.containsItem(2), equalTo(false));
        order.addItem(Items.item(2, "drink", 200), 1);
        assertThat(order.containsItem(2), equalTo(true));
    }

    @Test
    public void removeItem() {
        Order order = new Order("foo", 0.09);
        order.addItem(Items.item(1, "pizza", 1000), 1);
        assertThat(order.containsItem(1), equalTo(true));
        order.removeItem(1);
        assertThat(order.containsItem(1), equalTo(false));
    }

    @Test
    public void removeMissingItem() {
        Order order = new Order("foo", 0.09);
        assertThat(order.containsItem(1), equalTo(false));
        order.removeItem(1);
        assertThat(order.containsItem(1), equalTo(false));
    }

    @Test
    public void removeOneItem() {
        Order order = new Order("foo", 0.09);
        order.addItem(Items.item(1, "pizza", 1000), 1);
        order.addItem(Items.item(2, "drink", 200), 1);
        assertThat(order.containsItem(1), equalTo(true));
        assertThat(order.containsItem(2), equalTo(true));
        order.removeItem(1);
        assertThat(order.containsItem(1), equalTo(false));
        assertThat(order.containsItem(2), equalTo(true));
    }

    @Test
    public void updateItem() {
        Order order = new Order("foo", 0.09);
        order.addItem(Items.item(1, "pizza", 1000), 1);
        order.addItem(Items.item(2, "drink", 200), 1);
        assertThat(order.getTotal().subtotal, equalTo(1200));
        order.updateItem(1, 2, id -> null);
        assertThat(order.getTotal().subtotal, equalTo(2200));
    }

    @Test
    public void updateMissingItem() {
        Order order = new Order("foo", 0.09);
        assertThat(order.getTotal().subtotal, equalTo(0));
        order.updateItem(1, 2, id -> Items.item(id, "pizza", 1000));
        assertThat(order.getTotal().subtotal, equalTo(2000));
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateItemWithInvalidQuantity() {
        Order order = new Order("foo", 0.09);
        order.updateItem(1, 0, id -> null);
    }
}
