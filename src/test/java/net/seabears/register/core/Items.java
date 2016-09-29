package net.seabears.register.core;

import static org.mockito.BDDMockito.*;

public final class Items {
    private Items() {
        throw new UnsupportedOperationException("cannot instantiate " + getClass());
    }

    public static Quantity quantity(int id) {
        return quantity(id, 0);
    }

    public static Quantity quantity(int id, int amount) {
        final Quantity quantity = new Quantity();
        quantity.itemId = id;
        quantity.amount = amount;
        return quantity;
    }

    public static Item item(final int id, final String name, final int price) {
        return new Item() {
            @Override
            public int getId() {
                return id;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public int getPrice() {
                return price;
            }
        };
    }
}
