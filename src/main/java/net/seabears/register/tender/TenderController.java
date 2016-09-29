package net.seabears.register.tender;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import net.seabears.register.DocumentType;
import net.seabears.register.PaymentIdSupplier;
import net.seabears.register.tender.Payment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tender")
public class TenderController {
    @Autowired
    private PaymentIdSupplier paymentIdSupplier;

    @Autowired
    private Bucket bucket;

    @RequestMapping(value = "", method = RequestMethod.POST)
    public void pay(@RequestBody Payment payment) {
        final String id = paymentIdSupplier.get();
        final String key = DocumentType.TENDER + "_" + id;
        JsonObject tender = JsonObject.empty();
        tender.put("id", id);
        tender.put("amount", payment.amount);
        tender.put("method", payment.method);
        tender.put("order_id", DocumentType.ORDER + "_" + payment.orderId);
        bucket.insert(JsonDocument.create(key, tender));
    }
}

