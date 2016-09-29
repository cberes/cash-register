package net.seabears.register.couchbase;

import com.couchbase.client.java.document.json.JsonObject;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class ItemTest {
    private static JsonObject makeJson(String key, Object value) {
        final JsonObject json = JsonObject.empty();
        json.put(key, value);
        return json;
    }

    @Test
    public void getId() {
        assertThat(new Item(makeJson("id", 1)).getId(), equalTo(1));
    }

    @Test
    public void getName() {
        assertThat(new Item(makeJson("name", "foo")).getName(), equalTo("foo"));
    }

    @Test
    public void getPrice() {
        assertThat(new Item(makeJson("price", 100)).getPrice(), equalTo(100));
    }
}
