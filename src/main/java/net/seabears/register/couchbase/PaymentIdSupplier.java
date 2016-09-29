package net.seabears.register.couchbase;

import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.function.Supplier;

@Component
public class PaymentIdSupplier implements Supplier<String> {
    @Override
    public String get() {
        return UUID.randomUUID().toString();
    }
}
