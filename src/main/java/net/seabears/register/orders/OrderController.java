package net.seabears.register.orders;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.Query;
import com.couchbase.client.java.query.QueryResult;
import net.seabears.register.BadRequestException;
import net.seabears.register.DocumentType;
import net.seabears.register.NotFoundException;
import net.seabears.register.OrderIdSupplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Optional;
import java.util.OptionalInt;

import static com.couchbase.client.java.query.Select.select;
import static com.couchbase.client.java.query.dsl.Expression.i;
import static com.couchbase.client.java.query.dsl.Expression.s;
import static com.couchbase.client.java.query.dsl.Expression.x;
import static java.util.stream.IntStream.range;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private static final long MAX_ORDER_NUM = 100L;

    @Autowired
    private OrderIdSupplier orderIdSupplier;

    @Autowired
    private Bucket bucket;

    @RequestMapping(value = "", method = RequestMethod.PUT)
    public String newOrder() {
        final String id = orderIdSupplier.get();
        JsonObject order = buildOrder(id);
        bucket.insert(JsonDocument.create(getOrderKey(id), order));
        return id;
    }

    private JsonObject buildOrder(String id) {
        return buildOrder(id, 0);
    }

    private JsonObject buildOrder(String id, int number) {
        JsonObject order = JsonObject.empty();
        order.put("id", id);
        order.put("type", DocumentType.ORDER.toString());
        order.put("number", number);
        order.put("items", JsonArray.empty());
        return order;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.POST)
    public int addItem(@PathVariable String id, @RequestBody Quantity quantity) {
        final String key = getOrderKey(id);
        JsonDocument doc = bucket.get(key);
        if (doc == null) {
            throw new NotFoundException("order " + id + " not found");
        }

        JsonObject order = doc.content();
        if (order.getInt("number") != 0) {
            throw new BadRequestException("cannot modify a submitted order");
        }

        JsonArray items = order.getArray("items");
        boolean containsItem = range(0, items.size())
                .mapToObj(items::getObject)
                .anyMatch(obj -> obj.getInt("id") == quantity.itemId);
        if (containsItem) {
            throw new BadRequestException("item " + quantity.itemId + " is already on the order");
        }

        JsonObject item = JsonObject.empty();
        item.put("id", quantity.itemId);
        item.put("price", getItem(quantity.itemId).getInt("price"));
        item.put("quantity", 1);
        items.add(item);
        bucket.upsert(JsonDocument.create(key, order));
        return getOrderTotal(order);
    }

    private JsonObject getItem(int id) {
        JsonDocument itemDoc = bucket.get(DocumentType.ITEM + "_" + id);
        if (itemDoc == null) {
            throw new NotFoundException("item " + id + " not found");
        }
        return itemDoc.content();
    }

    private JsonObject buildOrderItem(String orderId, long itemId) {
        return buildOrderItem(orderId, itemId, 1);
    }

    private JsonObject buildOrderItem(String orderId, long itemId, int quantity) {
        JsonObject orderItem = JsonObject.empty();
        orderItem.put("order_id", DocumentType.ORDER + "_" + orderId);
        orderItem.put("item_id", DocumentType.ITEM + "_" + itemId);
        orderItem.put("type", DocumentType.ORDER_ITEM.toString());
        orderItem.put("quantity", quantity);
        return orderItem;
    }

    private String getOrderKey(String id) {
        return DocumentType.ORDER + "_" + id;
    }

    private String getOrderItemKey(String orderId, long itemId) {
        return String.format("%s_%s:%d", DocumentType.ORDER_ITEM, orderId, itemId);
    }

    private int getOrderTotal(JsonObject order) {
        final JsonArray items = order.getArray("items");
        return range(0, items.size())
                .mapToObj(items::getObject)
                .mapToInt(item -> item.getInt("price") * item.getInt("quantity"))
                .sum();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public int updateItem(@PathVariable String id, @RequestBody Quantity quantity) {
        final String key = getOrderKey(id);
        JsonDocument doc = bucket.get(key);
        if (doc == null) {
            throw new NotFoundException("order " + id + " not found");
        }

        JsonObject order = doc.content();
        if (order.getInt("number") != 0) {
            throw new BadRequestException("cannot modify a submitted order");
        }

        JsonArray items = order.getArray("items");
        Optional<JsonObject> optItem = range(0, items.size())
                .mapToObj(items::getObject)
                .filter(obj -> obj.getInt("id") == quantity.itemId)
                .findFirst();
        JsonObject item;
        if (optItem.isPresent()) {
            item = optItem.get();
        } else {
            item = JsonObject.empty();
            items.add(item);
            item.put("id", quantity.itemId);
            item.put("price", getItem(quantity.itemId).getInt("price"));
        }
        item.put("quantity", quantity.amount);
        bucket.upsert(JsonDocument.create(key, order));
        return getOrderTotal(order);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public int deleteItem(@PathVariable String id, @RequestBody Quantity quantity) {
        final String key = getOrderKey(id);
        JsonDocument doc = bucket.get(key);
        if (doc == null) {
            throw new NotFoundException("order " + id + " not found");
        }

        JsonObject order = doc.content();
        if (order.getInt("number") != 0) {
            throw new BadRequestException("cannot modify a submitted order");
        }

        final JsonArray items = order.getArray("items");
        final JsonArray newItems = JsonArray.empty();
        range(0, items.size())
                .mapToObj(items::getObject)
                .filter(obj -> obj.getInt("id") != quantity.itemId)
                .forEach(newItems::add);
        order.put("items", newItems);
        bucket.upsert(JsonDocument.create(key, order));
        return getOrderTotal(order);
    }

    @RequestMapping(value = "/{id}/submit", method = RequestMethod.POST)
    public int submit(@PathVariable String id) {
        final String key = getOrderKey(id);
        JsonDocument doc = bucket.get(key);
        if (doc == null) {
            throw new NotFoundException("order " + id + " not found");
        }

        JsonObject order = doc.content();
        if (order.getInt("number") != 0) {
            throw new BadRequestException("cannot modify a submitted order");
        }

        final int number = (int) (bucket.counter("order_number", 1).content() % MAX_ORDER_NUM + 1L);
        order.put("number", number);
        bucket.upsert(JsonDocument.create(key, order));
        return number;
    }
}

