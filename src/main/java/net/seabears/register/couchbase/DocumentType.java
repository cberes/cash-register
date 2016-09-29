package net.seabears.register.couchbase;

import java.util.Locale;

enum DocumentType {
    ITEM, ORDER, TENDER;

    public String toString() {
        return name().toLowerCase(Locale.ENGLISH);
    }
}
