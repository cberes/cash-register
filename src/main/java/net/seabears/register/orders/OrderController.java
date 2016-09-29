package net.seabears.register.orders;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.PersistTo;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.Query;
import com.couchbase.client.java.query.QueryResult;
import net.seabears.register.DocumentType;
import net.seabears.register.OrderIdSupplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static com.couchbase.client.java.query.Select.select;
import static com.couchbase.client.java.query.dsl.Expression.i;
import static com.couchbase.client.java.query.dsl.Expression.s;
import static com.couchbase.client.java.query.dsl.Expression.x;

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
        return order;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.POST)
    public int addItem(@PathVariable String id, @RequestBody Quantity quantity) {
        JsonObject orderItem = buildOrderItem(id, quantity.itemId);
        final String key = getOrderItemKey(id, quantity.itemId);
        bucket.insert(JsonDocument.create(key, orderItem), PersistTo.MASTER);
        return getOrderTotal(id);
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

    private int getOrderTotal(String orderId) {
        Query query = Query.simple(select("IFNULL(SUM(i.price * o.quantity), 0) as subtotal")
                .from(i(bucket.name()).as("o"))
                .innerJoin(i(bucket.name()).as("i") + " ON KEYS o.item_id")
                .where(x("o.type").eq(s(DocumentType.ORDER_ITEM.toString())).and(x("o.order_id").eq(s(DocumentType.ORDER + "_" + orderId)))));
        System.out.println(query.statement());
        QueryResult result = bucket.query(query);
        System.out.println(result.errors());
        System.out.println(result.finalSuccess());
        return result
                .allRows().get(0)
                .value().getInt("subtotal");
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public int updateItem(@PathVariable String id, @RequestBody Quantity quantity) {
        JsonObject orderItem = buildOrderItem(id, quantity.itemId, quantity.amount);
        final String key = getOrderItemKey(id, quantity.itemId);
        bucket.upsert(JsonDocument.create(key, orderItem), PersistTo.MASTER);
        return getOrderTotal(id);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public int deleteItem(@PathVariable String id, @RequestBody Quantity quantity) {
        final String key = getOrderItemKey(id, quantity.itemId);
        bucket.remove(key, PersistTo.MASTER);
        return getOrderTotal(id);
    }

    @RequestMapping(value = "/{id}/submit", method = RequestMethod.POST)
    public int submit(@PathVariable String id) {
        final int number = (int) (bucket.counter("order_number", 1).content() % MAX_ORDER_NUM + 1L);
        JsonObject order = buildOrder(id, number);
        bucket.upsert(JsonDocument.create(getOrderKey(id), order));
        return number;
    }
}

