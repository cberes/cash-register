package net.seabears.register.core;

import com.couchbase.client.java.document.json.JsonObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

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

    public static JsonObject itemJson(final int id, final String name, final int price) {
        return itemJson(item(id, name, price));
    }

    public static JsonObject itemJson(final Item item) {
        final JsonObject json = JsonObject.empty();
        json.put("id", item.getId());
        json.put("name", item.getName());
        json.put("price", item.getPrice());
        return json;
    }

    private static final ObjectMapper mapper = new ObjectMapper();

    public static Matcher<Item> isItem(final Item item) {
        return new BaseMatcher<Item>() {
            @Override
            public boolean matches(Object other) {
                return other instanceof Item
                        && ((Item) other).getId() == item.getId()
                        && ((Item) other).getName().equals(item.getName())
                        && ((Item) other).getPrice() == item.getPrice();
            }

            @Override
            public void describeTo(Description description) {
                try {
                    description.appendText("Expected ").appendValue(mapper.writeValueAsString(item));
                } catch (JsonProcessingException e) {
                    throw new IllegalStateException(e);
                }
            }

            @Override
            public void describeMismatch(Object other, Description description) {
                try {
                    description.appendText("was ").appendValue(mapper.writeValueAsString(other));
                } catch (JsonProcessingException e) {
                    throw new IllegalStateException(e);
                }
            }
        };
    }
}
