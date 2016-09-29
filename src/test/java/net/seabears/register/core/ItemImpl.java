package net.seabears.register.core;

public class ItemImpl implements Item {
    private final int id;
    private final String name;
    private final int price;

    public ItemImpl(int id, String name, int price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getPrice() {
        return price;
    }
}
