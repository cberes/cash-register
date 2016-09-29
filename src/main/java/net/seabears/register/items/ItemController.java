package net.seabears.register.items;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.Query;
import com.couchbase.client.java.query.QueryRow;
import net.seabears.register.DocumentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

import static com.couchbase.client.java.query.Select.select;
import static com.couchbase.client.java.query.dsl.Expression.i;
import static com.couchbase.client.java.query.dsl.Expression.s;
import static com.couchbase.client.java.query.dsl.Expression.x;

@RestController
@RequestMapping("/items")
public class ItemController {
    @Autowired
    private Bucket bucket;

    @RequestMapping("")
    public List<Item> items() {
        return bucket.query(Query.simple(select(i(bucket.name()) + ".*")
                .from(i(bucket.name()))
                .where(x("type").eq(s(DocumentType.ITEM.toString())))))
                .allRows()
                .stream()
                .map(ItemController::toItem)
                .collect(Collectors.toList());
    }

    private static Item toItem(QueryRow row) {
        final JsonObject json = row.value();
        final Item item = new Item();
        item.id = json.getLong("id");
        item.name = json.getString("name");
        item.price = json.getInt("price");
        return item;
    }
}

