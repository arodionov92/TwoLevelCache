import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.home.rodionov.cache.LRUAlgorithm;
import ru.home.rodionov.cache.RAMCache;

import java.util.ArrayList;
import java.util.List;

public class RAMCacheTest {
    private static final int ONE_SECOND = 1000;
    private static RAMCache rc;
    private static List<Object> keys;
    private static List<Object> values;

    @BeforeClass
    public static void setUp() {
        rc = new RAMCache(new LRUAlgorithm(), ONE_SECOND);
        keys = new ArrayList<>();
        values = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            keys.add(new String("key_" + i));
            values.add(new String("value_" + i));
        }
    }

    @Test
    public void testLRUAlgorithmInRamCache() {
        for (int i = 0; i < keys.size(); i++) {
            rc.add(keys.get(i), values.get(i));
        }
        rc.get(keys.get(5));
        Assert.assertEquals("Element is not shifted to first position", 0, rc.indexOf(keys.get(5)));
    }

    @Test
    public void testCacheInvalidation() throws InterruptedException {
        for (int i = 0; i < keys.size(); i++) {
            rc.add(keys.get(i), values.get(i));
        }
        int initialSize = rc.size();
        Thread.sleep(ONE_SECOND);
        Assert.assertTrue("Cache invalidation hasn't worked successfully", rc.size() < initialSize);
    }

}

