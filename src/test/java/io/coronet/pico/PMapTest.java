package io.coronet.pico;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;

public class PMapTest {

    @Test
    public void test_empty_isEmpty() {
        Assert.assertTrue(PMap.empty().isEmpty());
    }

    @Test
    public void test_empty_size() {
        Assert.assertEquals(0, PMap.empty().size());
    }

    @Test(expected = NullPointerException.class)
    public void test_empty_containsKey_null() {
        PMap.empty().containsKey(null);
    }

    @Test
    public void test_empty_containsKey() {
        PMap<String, Integer> empty = PMap.empty();
        Assert.assertFalse(empty.containsKey("Hello"));
        Assert.assertFalse(empty.containsKey(1));
    }

    @Test(expected = NullPointerException.class)
    public void test_empty_get_null() {
        PMap.empty().get(null);
    }

    @Test
    public void test_empty_get() {
        PMap<String, Integer> empty = PMap.empty();

        Assert.assertNull(empty.get("Hello"));
        Assert.assertNull(empty.get(1));

        Assert.assertEquals((Integer) 1, empty.getOrDefault("Hello", 1));
    }

    @Test(expected = NullPointerException.class)
    public void test_empty_remove_null() {
        PMap.empty().remove(null);
    }

    @Test
    public void test_empty_remove() {
        PMap<String, Integer> empty = PMap.empty();

        Assert.assertSame(empty, empty.remove("Hello"));
        Assert.assertSame(empty, empty.remove(1));
    }

    @Test
    public void test_empty_keySet() {
        PMap<String, Integer> empty = PMap.empty();
        Iterable<String> entries = empty.keySet();
        Assert.assertNotNull(entries);

        Iterator<String> iter = entries.iterator();

        Assert.assertNotNull(iter);
        Assert.assertFalse(iter.hasNext());
    }

    @Test
    public void test_empty_entrySet() {
        PMap<String, Integer> empty = PMap.empty();
        Iterable<PMap.Entry<String, Integer>> entries = empty.entrySet();
        Assert.assertNotNull(entries);

        Iterator<PMap.Entry<String, Integer>> iter = entries.iterator();

        Assert.assertNotNull(iter);
        Assert.assertFalse(iter.hasNext());
    }

    @Test
    public void test_empty_forEach() {
        PMap.empty().forEach((e) -> { Assert.fail("Unexpected action!"); });
        PMap.empty().forEach((k, v) -> { Assert.fail("Unexpected action!"); });
    }

    @Test
    public void test_empty_toString() {
        Assert.assertEquals("{}", PMap.empty().toString());
    }

    @Test
    public void test_put() {
        PMap<String, String> map = PMap.<String, String>empty()
                .put("Hello", "World")
                .put("foo", "bar")
                .put("bogus", "monkeys");

        Assert.assertFalse(map.isEmpty());
        Assert.assertEquals(3, map.size());

        Assert.assertTrue(map.containsKey("Hello"));
        Assert.assertTrue(map.containsKey("bogus"));
        Assert.assertFalse(map.containsKey("garbage"));
        Assert.assertFalse(map.containsKey("Foo"));
        Assert.assertFalse(map.containsKey("monkeys"));

        Assert.assertEquals("World", map.get("Hello"));
        Assert.assertEquals("bar", map.get("foo"));
        Assert.assertEquals("monkeys", map.getOrDefault("bogus", "wrong"));
        Assert.assertNull(map.get("wrong"));
        Assert.assertEquals("default", map.getOrDefault("nopesies", "default"));
    }

    @Test(expected = NullPointerException.class)
    public void test_put_nullKey() {
        PMap.empty().put(null, "OogBoog");
    }

    @Test
    public void test_put_nullValue() {
        PMap<String, String> map = PMap.<String, String>empty()
                .put("Hello", null);

        Assert.assertFalse(map.isEmpty());
        Assert.assertEquals(1, map.size());

        Assert.assertTrue(map.containsKey("Hello"));
        Assert.assertFalse(map.containsKey("bogus"));

        Assert.assertNull(map.get("Hello"));
        Assert.assertNull(map.getOrDefault("Hello", "default"));
    }

    @Test
    public void test_put_overwrite() {
        PMap<String, String> map = PMap.<String, String>empty()
                .put("Hello", "World")
                .put("Hello", "Something Else");

        Assert.assertFalse(map.isEmpty());
        Assert.assertEquals(1, map.size());
        Assert.assertEquals("Something Else", map.get("Hello"));
    }

    @Test
    public void test_put_overwriteSame() {
        PMap<String, String> map = PMap.<String, String>empty()
                .put("Hello", "World");
        PMap<String, String> map2 = map.put("Hello", "World");

        Assert.assertSame(map, map2);
    }

    @Test
    public void test_put_lots() {
        PMap<String, Integer> map = PMap.empty();
        for (int i = 0; i < 12345; ++i) {
            map = map.put(Integer.toString(i), i);
        }

        Assert.assertEquals(12345, map.size());

        for (int i = 0; i < 12345; ++i) {
            Assert.assertEquals((Integer) i, map.get(Integer.toString(i)));
        }

        PMap<String, Integer> map2 = map;
        for (int i = 0; i < 127; ++i) {
            map2 = map2.put(Integer.toString(i), i);
        }

        Assert.assertSame(map, map2);
    }

    @Test
    public void test_entrySet() {
        PMap<String, String> map = PMap.<String, String>empty()
                .put("Hello", "World")
                .put("oog", "boog");

        boolean seenHello = false;
        boolean seenOog = false;

        for (PMap.Entry<String, String> entry : map.entrySet()) {
            if ("Hello".equals(entry.getKey())) {
                Assert.assertEquals("World", entry.getValue());
                seenHello = true;
            } else if ("oog".equals(entry.getKey())) {
                Assert.assertEquals("boog", entry.getValue());
                seenOog = true;
            } else {
                Assert.fail("Unexpected key: " + entry.getKey());
            }
        }

        Assert.assertTrue(seenHello);
        Assert.assertTrue(seenOog);
    }

    @Test
    public void test_entrySet_lots() {
        PMap<String, Integer> map = PMap.empty();
        for (int i = 0; i < 12345; ++i) {
            map = map.put(Integer.toString(i), i);
        }

        Set<String> seen = new TreeSet<>();

        for (PMap.Entry<String, Integer> entry : map.entrySet()) {
            Assert.assertEquals(entry.getKey(), entry.getValue().toString());
            Assert.assertTrue(seen.add(entry.getKey()));
        }

        Assert.assertEquals(12345, seen.size());
        for (int i = 0; i < 12345; ++i) {
            Assert.assertTrue(seen.contains(Integer.toString(i)));
        }
    }
}
