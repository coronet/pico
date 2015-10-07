package io.coronet.pico;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.junit.Assert;
import org.junit.Test;

public class VectorTest {

    @Test
    public void test_empty_isEmpty() {
        List<String> vec = Vector.empty();
        Assert.assertTrue(vec.isEmpty());
    }

    @Test
    public void test_empty_size() {
        List<Integer> vec = Vector.empty();
        Assert.assertEquals(0, vec.size());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void test_empty_getMinus1() {
        Vector.empty().get(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void test_empty_get0() {
        Vector.empty().get(0);
    }

    @Test
    public void test_empty_iteratorHasNext() {
        Iterator<Exception> iter = Vector.<Exception>empty().iterator();
        Assert.assertFalse(iter.hasNext());
    }

    @Test(expected = NoSuchElementException.class)
    public void test_empty_iteratorNext() {
        Iterator<String> iter = Vector.<String>empty().iterator();
        iter.next();
    }

    @Test
    public void test_empty_contains() {
        List<Double> vec = Vector.empty();
        Assert.assertFalse(vec.contains(null));
        Assert.assertFalse(vec.contains("Hello World"));
    }


    @Test
    public void test_empty_indexOf() {
        List<String> vec = Vector.empty();
        Assert.assertEquals(-1, vec.indexOf("Hello World"));
    }

    @Test
    public void test_empty_indexOfNull() {
        List<String> vec = Vector.empty();
        Assert.assertEquals(-1, vec.indexOf(null));
    }

    @Test
    public void test_empty_lastIndexOf() {
        List<String> vec = Vector.empty();
        Assert.assertEquals(-1, vec.lastIndexOf("Hello World"));
    }

    @Test
    public void test_empty_lastIndexOfNull() {
        List<String> vec = Vector.empty();
        Assert.assertEquals(-1, vec.lastIndexOf(null));
    }

    @Test
    public void test_empty_asJavaCollection() {
        java.util.List<String> list = Vector.<String>empty().asJavaCollection();

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
    public void test_empty_singleton() {
        List<Boolean> vec = Vector.empty();
        Assert.assertSame(vec, Vector.empty());
    }

    @Test
    public void test_empty_add() {
        List<String> empty = Vector.empty();
        List<String> vec = empty.add("Hello World");

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
        Vector.empty().set(-1, "boo");
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void test_empty_set1() {
        Vector.empty().set(1, true);
    }

    @Test
    public void test_empty_set0() {
        List<String> empty = Vector.empty();
        List<String> vec = empty.set(0, "Hello World");

        Assert.assertTrue(empty.isEmpty());
        Assert.assertFalse(vec.isEmpty());

        Assert.assertEquals(1, vec.size());
        Assert.assertEquals("Hello World", vec.get(0));
    }

    @Test
    public void test_empty_toString() {
        Assert.assertEquals("[]", Vector.empty().toString());
    }

    @Test
    public void test_addMany() {
        List<Integer> vec = Vector.empty();
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
        List<Integer> vec = Vector.empty();
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
    public void test_null() {
        List<Double> vec = Vector.empty();
        vec = vec.add(1.0).add(null).add(2.0).add(null);

        Assert.assertEquals(4, vec.size());
        Assert.assertNull(vec.get(1));
        Assert.assertNull(vec.get(3));
        Assert.assertTrue(vec.contains(null));
        Assert.assertEquals(1, vec.indexOf(null));
        Assert.assertEquals(3, vec.lastIndexOf(null));
    }

    @Test
    public void test_plist_addAll() {
        List<Integer> vec = Vector.empty();
        vec = vec.addAll(Arrays.asList(0, 1, 2));
        vec = vec.addAll(vec);

        Assert.assertEquals(6, vec.size());
        for (int i = 0; i < 6; ++i) {
            Assert.assertEquals(i % 3, (int) vec.get(i));
        }
    }

    @Test
    public void test_toString() {
        List<Integer> vec = Vector.<Integer>empty()
            .addAll(Arrays.asList(1, 2, 3, 4));

        Assert.assertEquals("[1, 2, 3, 4]", vec.toString());
    }

    @Test
    public void test_hashCode() {
        Assert.assertEquals(
                Collections.emptyList().hashCode(),
                Vector.<Integer>empty().hashCode());

        Assert.assertEquals(
                Collections.singletonList(1).hashCode(),
                Vector.<Integer>empty().add(1).hashCode());

        Assert.assertEquals(
                Arrays.asList(1, 2).hashCode(),
                Vector.<Integer>empty()
                        .addAll(Arrays.asList(1, 2))
                        .hashCode());

        java.util.List<Integer> list = new ArrayList<>(123);
        List<Integer> plist = Vector.empty();

        for (int i = 0; i < 123; ++i) {
            list.add(i);
            plist = plist.add(i);
        }

        Assert.assertEquals(list.hashCode(), plist.hashCode());
    }

    @Test
    public void test_null_hashCode() {
        Assert.assertEquals(
                Collections.singletonList(null).hashCode(),
                Vector.empty().add(null).hashCode());
    }

    @Test
    public void test_empty_equals() {
        Assert.assertEquals(Vector.empty(), Vector.empty());
    }

    @Test
    public void test_equals() {
        List<Integer> one = Vector.<Integer>empty()
                .addAll(Arrays.asList(1, 2, 3));

        List<Integer> two = Vector.<Integer>empty()
                .add(1).add(2).add(3);

        Assert.assertEquals(one, two);
        Assert.assertEquals(one.set(2, null), two.set(2, null));

        Assert.assertNotEquals(one, two.set(2, null));
        Assert.assertNotEquals(one, two.add(4));
        Assert.assertNotEquals(one, two.first(2));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void test_empty_first() {
        Vector.empty().first();
    }

    @Test
    public void test_empty_first0() {
        Assert.assertSame(Vector.empty(), Vector.empty().first(0));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void test_firstMinus1() {
        Vector.empty().add(1).first(-1);
    }

    @Test
    public void test_firstOne() {
        List<?> one = Vector.empty().add(1);
        Assert.assertEquals(one, one.first(1));
    }

    @Test
    public void test_firstN() {
        for (int size = 0; size < 1230; ++size) {
            List<Integer> list = Vector.empty();

            for (int i = 0; i < size; ++i) {
                list = list.add(i);
            }

            for (int i = 0; i <= size; ++i) {
                List<Integer> first = list.first(i);
                Assert.assertEquals(i, first.size());

                for (int j = 0; j < i; ++j) {
                    Assert.assertEquals(j, (int) first.get(j));
                }
            }
        }
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void test_empty_firstOne() {
        Vector.empty().first(1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void test_empty_last() {
        Vector.empty().last();
    }

    @Test
    public void test_one_last() {
        Assert.assertEquals(1, Vector.empty().add(1).last());
    }

    @Test
    public void test_two_last() {
        Assert.assertEquals(2, Vector.empty().add(1).add(2).last());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void test_lastMinus1() {
        Vector.empty().add(1).last(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void test_empty_lastOne() {
        Vector.empty().last(1);
    }

    @Test
    public void test_lastN() {
        for (int size = 0; size < 1230; ++size) {
            List<Integer> list = Vector.empty();

            for (int i = 0; i < size; ++i) {
                list = list.add(i);
            }

            for (int i = 0; i <= size; ++i) {
                List<Integer> last = list.last(i);
                Assert.assertEquals(i, last.size());

                for (int j = 1; j <= i; ++j) {
                    Assert.assertEquals(size - j, (int) last.get(i - j));
                }
            }
        }
    }
}
