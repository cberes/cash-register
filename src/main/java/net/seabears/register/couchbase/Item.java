package net.seabears.register.couchbase;

import com.couchbase.client.java.document.json.JsonObject;

/**
 * Wrapper around a {@link com.couchbase.client.java.document.json.JsonObject} that stores the fields of the item.
 */
class Item implements net.seabears.register.core.Item {
    private final JsonObject json;

    Item(JsonObject json) {
        this.json = json;
    }

    @Override
    public int getId() {
        return json.getInt("id");
    }

    @Override
    public String getName() {
        return json.getString("name");
    }

    @Override
    public int getPrice() {
        return json.getInt("price");
    }
}
