import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.home.rodionov.cache.FileCache;
import ru.home.rodionov.cache.LRUAlgorithm;

import java.util.ArrayList;
import java.util.List;

public class FileCacheTest {
    private static final int ONE_SECOND = 1000;
    private static FileCache fc;
    private static List<Object> keys;
    private static List<Object> values;

    @BeforeClass
    public static void setUp() {
        fc = new FileCache("C:\\Users\\Rodionov\\Cache.fc\\", new LRUAlgorithm(), ONE_SECOND);
        keys = new ArrayList<>();
        values = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            keys.add(new String("key_" + i));
            values.add(new String("value_" + i));
        }
    }

    @Test
    public void testLRUAlgorithmInFileCache() {
        for (int i = 0; i < keys.size(); i++) {
            fc.add(keys.get(i), values.get(i));
        }
        fc.get(keys.get(5));
        Assert.assertEquals("Element is not shifted to first position", 0, fc.indexOf(keys.get(5)));
    }

    @Test
    public void testCacheInvalidation() throws InterruptedException {
        for (int i = 0; i < keys.size(); i++) {
            fc.add(keys.get(i), values.get(i));
        }
        int initialSize = fc.size();
        Thread.sleep(ONE_SECOND);
        Assert.assertTrue("Cache invalidation hasn't worked successfully", fc.size() < initialSize);
    }

}

