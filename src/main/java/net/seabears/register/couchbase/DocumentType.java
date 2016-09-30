package net.seabears.register.couchbase;

import java.util.Locale;

/**
 * Type of document.
 * Seems to be that in Couchbase documents are like rows.
 * All the documents are in a single bucket (usually).
 * These documents may be of different types (represented by this enum).
 */
enum DocumentType {
    /** The raw items that can be added in varying quantities to orders */
    ITEM,
    /** A collection of items and their quantities */
    ORDER,
    /** Payment for an order */
    TENDER;

    public String toString() {
        return name().toLowerCase(Locale.ENGLISH);
    }
}
