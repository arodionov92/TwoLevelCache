import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.home.rodionov.cache.FileCache;
import ru.home.rodionov.cache.LRUAlgorithm;
import ru.home.rodionov.cache.RAMCache;
import ru.home.rodionov.cache.TwoLevelCache;

import java.util.ArrayList;
import java.util.List;

public class TwoLevelCacheTest {
    private static final int ONE_SECOND = 1000;
    private static TwoLevelCache twc;
    private static List<Object> keys;
    private static List<Object> values;

    @BeforeClass
    public static void setUp() {
        String filepath = "C:\\Users\\Rodionov\\Cache.fc";
        RAMCache rc = new RAMCache(new LRUAlgorithm(), ONE_SECOND);
        FileCache fc = new FileCache(filepath, new LRUAlgorithm(), ONE_SECOND);
        twc = new TwoLevelCache(rc, fc, 10, 10);
        keys = new ArrayList<>();
        values = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            keys.add(new String("key_" + i));
            values.add(new String("value_" + i));
        }
    }

    @Test
    public void testLRUAlgorithmInFirstLevel() {
        for (int i = 0; i < keys.size(); i++) {
            twc.add(keys.get(i), values.get(i));
        }
        twc.get(keys.get(5));
        Assert.assertEquals("Element is not shifted to first position",0, twc.indexOf(keys.get(5)));
    }

    @Test
    public void testLRUAlgorithmInSecondLevel() {
        for (int i = 0; i < keys.size(); i++) {
            twc.add(keys.get(i), values.get(i));
        }
        twc.get(keys.get(14));
        Assert.assertEquals("Element is not shifted to first position",0, twc.indexOf(keys.get(14)));
    }

    @Test
    public void testCacheInvalidation() throws InterruptedException {
        for (int i = 0; i < keys.size(); i++) {
            twc.add(keys.get(i), values.get(i));
        }
        int initialSize = twc.size();
        Thread.sleep(ONE_SECOND);
        Assert.assertTrue(twc.size()<initialSize);
    }

}
