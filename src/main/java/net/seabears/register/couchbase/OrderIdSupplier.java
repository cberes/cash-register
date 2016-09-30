package net.seabears.register.couchbase;

import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.function.Supplier;

/** Generates random UUIDs to uniquely identify orders */
@Component
public class OrderIdSupplier implements Supplier<String> {
    @Override
    public String get() {
        return UUID.randomUUID().toString();
    }
}
