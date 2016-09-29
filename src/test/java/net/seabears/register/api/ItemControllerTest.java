package net.seabears.register.api;

import net.seabears.register.core.Item;
import net.seabears.register.core.Items;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ItemControllerTest extends AbstractControllerTest {
    @Test
    public void getItems() throws Exception {
        List<Item> expected = Arrays.asList(Items.item(1, "", 100), Items.item(2, "", 250));
        given(data.getItems()).willReturn(expected);
        mvc.perform(MockMvcRequestBuilders.get("/items")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(toJson(expected)));
    }
}
