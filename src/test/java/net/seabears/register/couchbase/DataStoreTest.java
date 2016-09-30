package net.seabears.register.couchbase;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.JsonLongDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.Query;
import com.couchbase.client.java.query.QueryResult;
import com.couchbase.client.java.query.QueryRow;
import net.seabears.register.core.Items;
import net.seabears.register.core.OrderConfig;
import net.seabears.register.core.Orders;
import net.seabears.register.core.Payment;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static net.seabears.register.core.Items.isItem;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@ImportAutoConfiguration
public class DataStoreTest {
    @Autowired
    private DataStore data;

    @MockBean
    private Bucket bucket;

    @MockBean
    private QueryResult result;

    @MockBean
    private OrderIdSupplier orderIdSupplier;

    @MockBean
    private PaymentIdSupplier paymentIdSupplier;

    private static QueryRow mockRow(JsonObject value) {
        final QueryRow row = mock(QueryRow.class);
        given(row.value()).willReturn(value);
        return row;
    }

    private static JsonDocument mockDoc(JsonObject value) {
        final JsonDocument doc = mock(JsonDocument.class);
        given(doc.content()).willReturn(value);
        return doc;
    }

    @Before
    public void setup() {
        given(bucket.name()).willReturn("test-bucket");
        given(bucket.query(any(Query.class))).willReturn(result);
    }

    private static void allItemsMatch(List<net.seabears.register.core.Item> expected, List<net.seabears.register.core.Item> actual) {
        range(0, expected.size()).forEach(i -> assertThat(actual.get(i), isItem(expected.get(i))));
    }

    @Test
    public void getItems() {
        List<net.seabears.register.core.Item> items = Arrays.asList(Items.item(1, "foo", 100), Items.item(2, "bar", 250));
        List<QueryRow> rows = items.stream().map(Items::itemJson).map(DataStoreTest::mockRow).collect(toList());
        given(result.allRows()).willReturn(rows);
        allItemsMatch(data.getItems(), items);
    }

    @Test
    public void getItem() {
        final net.seabears.register.core.Item item = Items.item(1, "foo", 100);
        final JsonDocument doc = mockDoc(Items.itemJson(item));
        given(bucket.get("item_1")).willReturn(doc);
        assertThat(data.getItem(1), isItem(item));
    }

    @Test
    public void getItemNotFound() {
        given(bucket.get(anyString())).willReturn(null);
        assertThat(data.getItem(1), nullValue());
    }

    @Test
    public void createPayment() {
        final String id = "51";
        final Payment expected = new Payment();
        expected.amount = 100;
        expected.method = "CASH";
        expected.orderId = "foo";
        given(paymentIdSupplier.get()).willReturn(id);
        data.createPayment(expected);
        ArgumentCaptor<JsonDocument> doc = ArgumentCaptor.forClass(JsonDocument.class);
        verify(bucket).insert(doc.capture());
        assertThat(doc.getValue().content().getString("id"), equalTo(id));
        assertThat(doc.getValue().content().getInt("amount"), equalTo(expected.amount));
        assertThat(doc.getValue().content().getString("method"), equalTo(expected.method));
        assertThat(doc.getValue().content().getString("order_id"), equalTo("order_" + expected.orderId));
    }

    private JsonObject makePayment(String id, String orderId, int amount) {
        JsonObject json = JsonObject.empty();
        json.put("id", id);
        json.put("order_id", "order_" + orderId);
        json.put("amount", amount);
        json.put("method", "CASH");
        return json;
    }

    @Test
    public void getTotalPaid() {
        final String orderId = "51";
        List<JsonObject> payments = Arrays.asList(makePayment("1", orderId, 100), makePayment("2", orderId, 250));
        List<QueryRow> rows = payments.stream().map(DataStoreTest::mockRow).collect(toList());
        given(result.allRows()).willReturn(rows);
        assertThat(data.getTotalPaid(orderId), equalTo(350));
    }

    @Test
    public void createOrder() {
        final String id = "51";
        final double tax = 0.09;
        final OrderConfig expected = Orders.config(tax);
        given(orderIdSupplier.get()).willReturn(id);
        final Order actual = data.createOrder(expected);
        ArgumentCaptor<JsonDocument> doc = ArgumentCaptor.forClass(JsonDocument.class);
        verify(bucket).insert(doc.capture());
        assertThat(doc.getValue().content().getString("id"), equalTo(id));
        assertEquals(doc.getValue().content().getDouble("tax"), tax, 1E-9);
        assertThat(actual.getId(), equalTo(id));
        assertThat(actual.isSubmitted(), equalTo(false));
    }

    @Test
    public void getOrder() {
        final JsonObject expected = JsonObject.empty();
        final JsonDocument doc = mockDoc(expected);
        given(bucket.get("order_1")).willReturn(doc);
        assertThat(data.getOrder("1").json, is(expected));
    }

    @Test
    public void getOrderNotFound() {
        given(bucket.get(anyString())).willReturn(null);
        assertThat(data.getOrder("1"), nullValue());
    }

    @Test
    public void updateOrder() {
        final String id = "51";
        final JsonObject json = JsonObject.empty();
        data.updateOrder(id, new Order(json));
        ArgumentCaptor<JsonDocument> doc = ArgumentCaptor.forClass(JsonDocument.class);
        verify(bucket).upsert(doc.capture());
        assertThat(doc.getValue().id(), equalTo("order_" + id));
        assertThat(doc.getValue().content(), is(json));
    }

    @Test
    public void incrementAndGetOrderNumber() {
        final JsonLongDocument json = mock(JsonLongDocument.class);
        given(bucket.counter("order_number", 1)).willReturn(json);
        given(json.content()).willReturn(0L);
        assertThat(data.incrementAndGetOrderNumber(), equalTo(1));
        given(json.content()).willReturn(1L);
        assertThat(data.incrementAndGetOrderNumber(), equalTo(2));
        given(json.content()).willReturn(99L);
        assertThat(data.incrementAndGetOrderNumber(), equalTo(100));
        given(json.content()).willReturn(100L);
        assertThat(data.incrementAndGetOrderNumber(), equalTo(1));
        given(json.content()).willReturn(101L);
        assertThat(data.incrementAndGetOrderNumber(), equalTo(2));
    }
}

