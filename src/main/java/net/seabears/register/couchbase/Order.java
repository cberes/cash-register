package net.seabears.register.couchbase;

import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import net.seabears.register.core.Item;
import net.seabears.register.core.OrderTotal;

import java.util.Optional;
import java.util.function.Function;

import static java.util.stream.IntStream.range;

class Order implements net.seabears.register.core.Order {
    final JsonObject json;

    Order(JsonObject json) {
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
    public void addItem(Item item, int quantity) {
        final JsonArray items = getItems();
        final JsonObject jsonItem = JsonObject.empty();
        jsonItem.put("id", item.id);
        jsonItem.put("price", item.price);
        jsonItem.put("quantity", quantity);
        items.add(jsonItem);
    }

    @Override
    public void updateItem(int id, int quantity, Function<Integer, Item> itemFunc) {
        final Optional<JsonObject> item = findItem(id);
        if (item.isPresent()) {
            item.get().put("quantity", quantity);
        } else {
            addItem(itemFunc.apply(id), quantity);
        }
    }
}
