package io.coronet.pico;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.Assert;
import org.junit.Test;

public class PQueueTest {

    @Test
    public void test_empty_isEmpty() {
        PList<String> vec = PQueueImpl.empty();
        Assert.assertTrue(vec.isEmpty());
    }

    @Test
    public void test_empty_size() {
        PList<Integer> vec = PQueueImpl.empty();
        Assert.assertEquals(0, vec.size());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void test_empty_getMinus1() {
        PQueueImpl.empty().get(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void test_empty_get0() {
        PQueueImpl.empty().get(0);
    }

    @Test
    public void test_empty_iteratorHasNext() {
        Iterator<Exception> iter = PQueueImpl.<Exception>empty().iterator();
        Assert.assertFalse(iter.hasNext());
    }

    @Test(expected = NoSuchElementException.class)
    public void test_empty_iteratorNext() {
        Iterator<String> iter = PQueueImpl.<String>empty().iterator();
        iter.next();
    }

    @Test
    public void test_empty_contains() {
        PList<Double> vec = PQueueImpl.empty();
        Assert.assertFalse(vec.contains(null));
        Assert.assertFalse(vec.contains("Hello World"));
    }


    @Test
    public void test_empty_indexOf() {
        PList<String> vec = PQueueImpl.empty();
        Assert.assertEquals(-1, vec.indexOf("Hello World"));
    }

    @Test
    public void test_empty_indexOfNull() {
        PList<String> vec = PQueueImpl.empty();
        Assert.assertEquals(-1, vec.indexOf(null));
    }

    @Test
    public void test_empty_lastIndexOf() {
        PList<String> vec = PQueueImpl.empty();
        Assert.assertEquals(-1, vec.lastIndexOf("Hello World"));
    }

    @Test
    public void test_empty_lastIndexOfNull() {
        PList<String> vec = PQueueImpl.empty();
        Assert.assertEquals(-1, vec.lastIndexOf(null));
    }

    @Test
    public void test_empty_asJavaCollection() {
        List<String> list = PQueueImpl.<String>empty().asJavaCollection();

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
        PList<Boolean> vec = PQueueImpl.empty();
        Assert.assertSame(vec, PQueueImpl.empty());
    }

    @Test
    public void test_empty_add() {
        PList<String> empty = PQueueImpl.empty();
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
        PQueueImpl.empty().set(-1, "boo");
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void test_empty_set1() {
        PQueueImpl.empty().set(1, true);
    }

    @Test
    public void test_empty_set0() {
        PList<String> empty = PQueueImpl.empty();
        PList<String> vec = empty.set(0, "Hello World");

        Assert.assertTrue(empty.isEmpty());
        Assert.assertFalse(vec.isEmpty());

        Assert.assertEquals(1, vec.size());
        Assert.assertEquals("Hello World", vec.get(0));
    }

    @Test
    public void test_empty_toString() {
        Assert.assertEquals("[]", PQueueImpl.empty().toString());
    }

    @Test
    public void test_addMany() {
        PList<Integer> vec = PQueueImpl.empty();
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
        PList<Integer> vec = PQueueImpl.empty();
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
        PList<Double> vec = PQueueImpl.empty();
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
        PList<Integer> vec = PQueueImpl.empty();
        vec = vec.addAll(Arrays.asList(0, 1, 2));
        vec = vec.addAll(vec);

        Assert.assertEquals(6, vec.size());
        for (int i = 0; i < 6; ++i) {
            Assert.assertEquals(i % 3, (int) vec.get(i));
        }
    }

    @Test
    public void test_toString() {
        PList<Integer> vec = PQueueImpl.<Integer>empty()
            .addAll(Arrays.asList(1, 2, 3, 4));

        Assert.assertEquals("[1, 2, 3, 4]", vec.toString());
    }

    @Test
    public void test_hashCode() {
        Assert.assertEquals(
                Collections.emptyList().hashCode(),
                PQueueImpl.<Integer>empty().hashCode());

        Assert.assertEquals(
                Collections.singletonList(1).hashCode(),
                PQueueImpl.<Integer>empty().add(1).hashCode());

        Assert.assertEquals(
                Arrays.asList(1, 2).hashCode(),
                PQueueImpl.<Integer>empty()
                        .addAll(Arrays.asList(1, 2))
                        .hashCode());

        List<Integer> list = new ArrayList<>(123);
        PList<Integer> plist = PQueueImpl.empty();

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
                PQueue.empty().add(null).hashCode());
    }

    @Test
    public void test_empty_equals() {
        Assert.assertEquals(PQueueImpl.empty(), PQueueImpl.empty());
    }

    @Test
    public void test_equals() {
        PList<Integer> one = PQueue.<Integer>empty()
                .addAll(Arrays.asList(1, 2, 3));

        PList<Integer> two = PQueue.<Integer>empty()
                .add(1).add(2).add(3);

        Assert.assertEquals(one, two);
        Assert.assertEquals(one.set(2, null), two.set(2, null));

        Assert.assertNotEquals(one, two.set(2, null));
        Assert.assertNotEquals(one, two.add(4));
        Assert.assertNotEquals(one, two.first(2));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void test_empty_first() {
        PQueue.empty().first();
    }

    @Test
    public void test_empty_first0() {
        Assert.assertSame(PQueue.empty(), PQueue.empty().first(0));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void test_firstMinus1() {
        PQueue.empty().add(1).first(-1);
    }

    @Test
    public void test_firstOne() {
        PList<?> one = PQueue.empty().add(1);
        Assert.assertEquals(one, one.first(1));
    }

    @Test
    public void test_firstN() {
        for (int size = 0; size < 1230; ++size) {
            PList<Integer> list = PQueue.empty();

            for (int i = 0; i < size; ++i) {
                list = list.add(i);
            }

            for (int i = 0; i <= size; ++i) {
                PList<Integer> first = list.first(i);
                Assert.assertEquals(i, first.size());

                for (int j = 0; j < i; ++j) {
                    Assert.assertEquals(j, (int) first.get(j));
                }
            }
        }
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void test_empty_firstOne() {
        PQueue.empty().first(1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void test_empty_last() {
        PQueue.empty().last();
    }

    @Test
    public void test_one_last() {
        Assert.assertEquals(1, PQueue.empty().add(1).last());
    }

    @Test
    public void test_two_last() {
        Assert.assertEquals(2, PQueue.empty().add(1).add(2).last());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void test_lastMinus1() {
        PQueue.empty().add(1).last(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void test_empty_lastOne() {
        PQueue.empty().last(1);
    }

    @Test
    public void test_lastN() {
        for (int size = 0; size < 1230; ++size) {
            PList<Integer> list = PQueue.empty();

            for (int i = 0; i < size; ++i) {
                list = list.add(i);
            }

            for (int i = 0; i <= size; ++i) {
                PList<Integer> last = list.last(i);
                Assert.assertEquals(i, last.size());

                for (int j = 1; j <= i; ++j) {
                    Assert.assertEquals(size - j, (int) last.get(i - j));
                }
            }
        }
    }
}
