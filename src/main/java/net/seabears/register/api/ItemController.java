package net.seabears.register.api;

import net.seabears.register.core.DataStore;
import net.seabears.register.core.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Here be operations on items */
@RestController
@RequestMapping("/items")
public class ItemController {
    @Autowired
    private DataStore data;

    /** Returns the list of all items along with some information: ID, price, and name. */
    @RequestMapping("")
    public List<Item> items() {
        return data.getItems();
    }
}

