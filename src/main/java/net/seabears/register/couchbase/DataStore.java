package net.seabears.register.couchbase;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.Query;
import com.couchbase.client.java.query.QueryRow;
import net.seabears.register.core.DocumentType;
import net.seabears.register.core.Item;
import net.seabears.register.core.Payment;
import net.seabears.register.core.Tax;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.couchbase.client.java.query.Select.select;
import static com.couchbase.client.java.query.dsl.Expression.i;
import static com.couchbase.client.java.query.dsl.Expression.s;
import static com.couchbase.client.java.query.dsl.Expression.x;

@Component
public class DataStore implements net.seabears.register.core.DataStore {
    private static class Keys {
        static String item(int id) {
            return DocumentType.ITEM + "_" + id;
        }

        static String order(String id) {
            return DocumentType.ORDER + "_" + id;
        }

        static String tender(String id) {
            return DocumentType.TENDER + "_" + id;
        }
    }

    @Value("${order.num.max}")
    private long orderNumMax;

    @Autowired
    private Bucket bucket;

    @Autowired
    private OrderIdSupplier orderIdSupplier;

    @Autowired
    private PaymentIdSupplier paymentIdSupplier;

    @Override
    public List<Item> getItems() {
        return bucket.query(Query.simple(select(i(bucket.name()) + ".*")
                .from(i(bucket.name()))
                .where(x("type").eq(s(DocumentType.ITEM.toString())))))
                .allRows()
                .stream()
                .map(QueryRow::value)
                .map(DataStore::jsonToItem)
                .collect(Collectors.toList());
    }

    private static Item jsonToItem(JsonObject json) {
        final Item item = new Item();
        item.id = json.getInt("id");
        item.name = json.getString("name");
        item.price = json.getInt("price");
        return item;
    }

    @Override
    public Item getItem(int id) {
        return Optional.ofNullable(bucket.get(Keys.item(id)))
                .map(JsonDocument::content)
                .map(DataStore::jsonToItem)
                .orElse(null);
    }

    @Override
    public void createPayment(Payment payment) {
        final String id = paymentIdSupplier.get();
        final String key = Keys.tender(id);
        JsonObject tender = paymentToJson(payment);
        tender.put("id", id);
        bucket.insert(JsonDocument.create(key, tender));
    }

    private static JsonObject paymentToJson(Payment payment) {
        JsonObject json = JsonObject.empty();
        json.put("amount", payment.amount);
        json.put("method", payment.method);
        json.put("order_id", Keys.order(payment.orderId));
        return json;
    }

    @Override
    public Order createOrder(Tax tax) {
        final JsonObject order = JsonObject.empty();
        final String id = orderIdSupplier.get();
        order.put("id", id);
        order.put("type", DocumentType.ORDER.toString());
        order.put("tax", tax.tax);
        order.put("number", 0);
        order.put("items", JsonArray.empty());
        bucket.insert(JsonDocument.create(Keys.order(id), order));
        return new Order(order);
    }

    @Override
    public Order getOrder(String id) {
        return Optional.ofNullable(bucket.get(Keys.order(id)))
                .map(JsonDocument::content)
                .map(Order::new)
                .orElse(null);
    }

    @Override
    public void updateOrder(String id, net.seabears.register.core.Order order) {
        bucket.upsert(JsonDocument.create(Keys.order(id), ((Order) order).json));
    }

    @Override
    public int incrementAndGetOrderNumber() {
        return (int) (bucket.counter("order_number", 1).content() % orderNumMax + 1L);
    }
}
