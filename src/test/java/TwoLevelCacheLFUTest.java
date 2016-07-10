import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.home.rodionov.cache.FileCache;
import ru.home.rodionov.cache.LFUAlgorithm;
import ru.home.rodionov.cache.RAMCache;
import ru.home.rodionov.cache.TwoLevelCache;

import java.util.ArrayList;
import java.util.List;

public class TwoLevelCacheLFUTest {
    private static final int ONE_SECOND = 1000;
    private static TwoLevelCache twc;
    private static List<Object> keys;
    private static List<Object> values;

    @BeforeClass
    public static void setUp() {
        String filepath = "C:\\Users\\FaiFlay\\Cache.fc";
        RAMCache rc = new RAMCache(new LFUAlgorithm(), ONE_SECOND);
        FileCache fc = new FileCache(filepath, new LFUAlgorithm(), ONE_SECOND);
        twc = new TwoLevelCache(rc, fc, 10, 10);
        keys = new ArrayList<>();
        values = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            keys.add(new String("key_" + i));
            values.add(new String("value_" + i));
        }
    }

    @Test
    public void testLFUAlgorithmInFirstLevel() {
        for (int i = 0; i < keys.size(); i++) {
            twc.add(keys.get(i), values.get(i));
        }
        twc.get("key_5");
        Assert.assertEquals("", 4, twc.indexOf("key_5"));
    }

    @Test
    public void testLFUAlgorithmInSecondLevel() {
        for (int i = 0; i < keys.size(); i++) {
            twc.add(keys.get(i), values.get(i));
        }
        twc.get("key_15");
        Assert.assertEquals("", 12, twc.indexOf("key_15"));
    }

    @Test
    public void testSwitchBetweenCaches() {
        for (int i = 0; i < keys.size(); i++) {
            twc.add(keys.get(i), values.get(i));
        }
        twc.get("key_18");
        Assert.assertEquals("", 8, twc.indexOf("key_18"));
    }


}
