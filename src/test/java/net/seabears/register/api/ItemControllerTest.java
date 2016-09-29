package net.seabears.register.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.seabears.register.core.DataStore;
import net.seabears.register.core.Item;
import net.seabears.register.core.ItemImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ItemControllerTest {
    @MockBean
    private DataStore data;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Test
    public void getItems() throws Exception {
        List<Item> expected = Arrays.asList(new ItemImpl(1, "", 100), new ItemImpl(2, "", 250));
        given(data.getItems()).willReturn(expected);
        mvc.perform(MockMvcRequestBuilders.get("/items").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expected)));
    }
}

