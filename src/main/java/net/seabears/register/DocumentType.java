package net.seabears.register;

import java.util.Locale;

public enum DocumentType {
    ITEM, ORDER, ORDER_ITEM, TENDER;

    public String toString() {
        return name().toLowerCase(Locale.ENGLISH);
    }
}
