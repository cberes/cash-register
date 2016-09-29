package net.seabears.register.api;

import net.seabears.register.core.DataStore;
import net.seabears.register.core.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemController {
    @Autowired
    private DataStore data;

    @RequestMapping("")
    public List<Item> items() {
        return data.getItems();
    }
}

