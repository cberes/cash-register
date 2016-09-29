package net.seabears.register.couchbase;

import com.couchbase.client.java.Bucket;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.BDDMockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@ImportAutoConfiguration
public class DataStoreTest {
    @MockBean
    private Bucket bucket;

    @Test
    public void test() {
    }
}

