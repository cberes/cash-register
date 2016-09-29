package net.seabears.register.core;

import java.util.function.Function;

public interface Order {
    String getId();
    boolean isSubmitted();
    void setNumber(int number);
    int getTotal();
    void removeItem(int id);
    boolean containsItem(int id);
    void addItem(Item item, int quantity);
    void updateItem(int id, int quantity, Function<Integer, Item> itemFunc);
}
