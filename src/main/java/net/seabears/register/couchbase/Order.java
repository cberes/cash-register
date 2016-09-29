package net.seabears.register.couchbase;

import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import net.seabears.register.core.Item;
import net.seabears.register.core.OrderTotal;
import org.springframework.util.Assert;

import javax.validation.constraints.NotNull;
import java.util.Optional;
import java.util.function.Function;

import static java.util.stream.IntStream.range;

class Order implements net.seabears.register.core.Order {
    final JsonObject json;

    Order(String id, double tax) {
        this(JsonObject.empty());
        Assert.hasText(id, "id must have text");
        Assert.isTrue(tax >= 0.0, "tax cannot be negative");
        json.put("id", id);
        json.put("type", DocumentType.ORDER.toString());
        json.put("tax", tax);
        json.put("number", 0);
        json.put("items", JsonArray.empty());
    }

    Order(@NotNull JsonObject json) {
        this.json = json;
    }

    @Override
    public String getId() {
        return json.getString("id");
    }

    @Override
    public boolean isSubmitted() {
        return json.getInt("number") != 0;
    }

    @Override
    public void setNumber(int number) {
        Assert.isTrue(number >= 0, "number cannot be negative");
        json.put("number", number);
    }

    @Override
    public OrderTotal getTotal() {
        final OrderTotal total = new OrderTotal();
        final JsonArray items = getItems();
        total.subtotal = range(0, items.size())
                .mapToObj(items::getObject)
                .mapToInt(item -> item.getInt("price") * item.getInt("quantity"))
                .sum();
        total.tax = (int) Math.floor(total.subtotal * json.getDouble("tax"));
        return total;
    }

    private JsonArray getItems() {
        return json.getArray("items");
    }

    @Override
    public void removeItem(final int id) {
        final JsonArray items = getItems();
        final JsonArray newItems = JsonArray.empty();
        range(0, items.size())
                .mapToObj(items::getObject)
                .filter(obj -> obj.getInt("id") != id)
                .forEach(newItems::add);
        json.put("items", newItems);
    }

    @Override
    public boolean containsItem(int id) {
        return findItem(id).isPresent();
    }

    private Optional<JsonObject> findItem(final int id) {
        final JsonArray items = getItems();
        return range(0, items.size())
                .mapToObj(items::getObject)
                .filter(obj -> obj.getInt("id") == id)
                .findFirst();
    }

    @Override
    public void addItem(@NotNull Item item, int quantity) {
        Assert.isTrue(quantity > 0, "quantity must be positive");
        final JsonArray items = getItems();
        final JsonObject jsonItem = JsonObject.empty();
        jsonItem.put("id", item.getId());
        jsonItem.put("price", item.getPrice());
        jsonItem.put("quantity", quantity);
        items.add(jsonItem);
    }

    @Override
    public void updateItem(int id, int quantity, @NotNull Function<Integer, Item> itemFunc) {
        Assert.isTrue(quantity > 0, "quantity must be positive");
        final Optional<JsonObject> item = findItem(id);
        if (item.isPresent()) {
            item.get().put("quantity", quantity);
        } else {
            addItem(itemFunc.apply(id), quantity);
        }
    }
}
