package io.coronet.pico;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.Assert;
import org.junit.Test;

public class PVectorTest {

    @Test
    public void test_empty_isEmpty() {
        PList<String> vec = PVector.empty();
        Assert.assertTrue(vec.isEmpty());
    }

    @Test
    public void test_empty_size() {
        PList<Integer> vec = PVector.empty();
        Assert.assertEquals(0, vec.size());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void test_empty_getMinus1() {
        PVector.empty().get(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void test_empty_get0() {
        PVector.empty().get(0);
    }

    @Test
    public void test_empty_iteratorHasNext() {
        Iterator<Exception> iter = PVector.<Exception>empty().iterator();
        Assert.assertFalse(iter.hasNext());
    }

    @Test(expected = NoSuchElementException.class)
    public void test_empty_iteratorNext() {
        Iterator<String> iter = PVector.<String>empty().iterator();
        iter.next();
    }

    @Test
    public void test_empty_contains() {
        PList<Double> vec = PVector.empty();
        Assert.assertFalse(vec.contains(null));
        Assert.assertFalse(vec.contains("Hello World"));
    }


    @Test
    public void test_empty_indexOf() {
        PList<String> vec = PVector.empty();
        Assert.assertEquals(-1, vec.indexOf("Hello World"));
    }

    @Test
    public void test_empty_indexOfNull() {
        PList<String> vec = PVector.empty();
        Assert.assertEquals(-1, vec.indexOf(null));
    }

    @Test
    public void test_empty_lastIndexOf() {
        PList<String> vec = PVector.empty();
        Assert.assertEquals(-1, vec.lastIndexOf("Hello World"));
    }

    @Test
    public void test_empty_lastIndexOfNull() {
        PList<String> vec = PVector.empty();
        Assert.assertEquals(-1, vec.lastIndexOf(null));
    }

    @Test
    public void test_empty_asJavaCollection() {
        List<String> list = PVector.<String>empty().asJavaCollection();

        Assert.assertTrue(list.isEmpty());
        Assert.assertEquals(0, list.size());

        try {
            list.add("boo");
            Assert.fail("expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            // Expected.
        }
    }

    @Test
    public void test_empty_clear() {
        PList<Boolean> vec = PVector.empty();
        Assert.assertSame(vec, vec.clear());
    }

    @Test
    public void test_empty_add() {
        PList<String> empty = PVector.empty();
        PList<String> vec = empty.add("Hello World");

        Assert.assertTrue(empty.isEmpty());
        Assert.assertFalse(vec.isEmpty());
        Assert.assertEquals(1, vec.size());

        Assert.assertEquals("Hello World", vec.get(0));

        Iterator<String> iter = vec.iterator();
        Assert.assertTrue(iter.hasNext());
        Assert.assertEquals("Hello World", iter.next());
        Assert.assertFalse(iter.hasNext());

        Assert.assertTrue(vec.contains("Hello World"));
        Assert.assertEquals(0, vec.indexOf("Hello World"));
        Assert.assertEquals(0, vec.lastIndexOf("Hello World"));

        Assert.assertEquals(-1, vec.indexOf(null));
        Assert.assertEquals(-1, vec.indexOf(123));
        Assert.assertEquals(-1, vec.lastIndexOf(null));
        Assert.assertEquals(-1, vec.lastIndexOf(123));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void test_empty_setMinus1() {
        PVector.empty().set(-1, "boo");
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void test_empty_set1() {
        PVector.empty().set(1, true);
    }

    @Test
    public void test_empty_set0() {
        PList<String> empty = PVector.empty();
        PList<String> vec = empty.set(0, "Hello World");

        Assert.assertTrue(empty.isEmpty());
        Assert.assertFalse(vec.isEmpty());

        Assert.assertEquals(1, vec.size());
        Assert.assertEquals("Hello World", vec.get(0));
    }

    @Test
    public void test_empty_toString() {
        Assert.assertEquals("[]", PVector.empty().toString());
    }

    @Test
    public void test_addMany() {
        PList<Integer> vec = PVector.empty();
        for (int i = 0; i < 12345; ++i) {
            vec = vec.add(i);
        }

        Assert.assertFalse(vec.isEmpty());
        Assert.assertEquals(12345, vec.size());

        for (int i = 0; i < 12345; ++i) {
            Assert.assertEquals(i, (int) vec.get(i));
        }

        int i = 0;
        for (Integer j : vec) {
            Assert.assertEquals(i, (int) j);
            i++;
        }
    }

    @Test
    public void test_setMany() {
        PList<Integer> vec = PVector.empty();
        vec = vec.addAll(Arrays.asList(new Integer[12345]));

        Assert.assertEquals(12345, vec.size());

        for (int i = 0; i < 12345; ++i) {
            vec = vec.set(12344 - i, i);
        }

        for (int i = 0; i < 12345; ++i) {
            Assert.assertEquals(12344 - i, (int) vec.get(i));
        }
    }

    @Test
    public void test_toString() {
        PList<Integer> vec = PVector.<Integer>empty()
            .addAll(Arrays.asList(1, 2, 3, 4));

        Assert.assertEquals("[1, 2, 3, 4]", vec.toString());
    }
}
