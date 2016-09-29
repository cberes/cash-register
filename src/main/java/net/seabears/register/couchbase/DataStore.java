package net.seabears.register.couchbase;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.Query;
import com.couchbase.client.java.query.QueryRow;
import net.seabears.register.core.OrderConfig;
import net.seabears.register.core.Payment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

import static com.couchbase.client.java.query.Select.select;
import static com.couchbase.client.java.query.dsl.Expression.i;
import static com.couchbase.client.java.query.dsl.Expression.s;
import static com.couchbase.client.java.query.dsl.Expression.x;
import static java.util.stream.Collectors.toList;

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
    public List<net.seabears.register.core.Item> getItems() {
        return bucket.query(Query.simple(select(i(bucket.name()) + ".*")
                .from(i(bucket.name()))
                .where(x("type").eq(s(DocumentType.ITEM.toString())))))
                .allRows()
                .stream()
                .map(QueryRow::value)
                .map(Item::new)
                .collect(toList());
    }

    @Override
    public net.seabears.register.core.Item getItem(int id) {
        return Optional.ofNullable(bucket.get(Keys.item(id)))
                .map(JsonDocument::content)
                .map(Item::new)
                .orElse(null);
    }

    @Override
    public void createPayment(@NotNull Payment payment) {
        final String id = paymentIdSupplier.get();
        JsonObject tender = paymentToJson(id, payment);
        bucket.insert(JsonDocument.create(Keys.tender(id), tender));
    }

    private static JsonObject paymentToJson(String id, Payment payment) {
        JsonObject json = JsonObject.empty();
        json.put("id", id);
        json.put("amount", payment.amount);
        json.put("method", payment.method);
        json.put("order_id", Keys.order(payment.orderId));
        return json;
    }

    @Override
    public Order createOrder(@NotNull OrderConfig orderConfig) {
        final String id = orderIdSupplier.get();
        final Order order = new Order(id, orderConfig.tax);
        bucket.insert(JsonDocument.create(Keys.order(id), order.json));
        return order;
    }

    @Override
    public Order getOrder(@NotNull String id) {
        return Optional.ofNullable(bucket.get(Keys.order(id)))
                .map(JsonDocument::content)
                .map(Order::new)
                .orElse(null);
    }

    @Override
    public void updateOrder(@NotNull String id, @NotNull net.seabears.register.core.Order order) {
        bucket.upsert(JsonDocument.create(Keys.order(id), ((Order) order).json));
    }

    @Override
    public int incrementAndGetOrderNumber() {
        return (int) (bucket.counter("order_number", 1).content() % orderNumMax + 1L);
    }
}
