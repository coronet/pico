package io.coronet.pico;

import java.util.Collections;
import java.util.Iterator;

/**
 * A "test" that ensures the set of methods that must be implemented by a
 * class extending AbstractPCollection doesn't grow.
 */
public class TestPCollection<T>
        extends AbstractPCollection<T, TestPCollection<T>> {

    @Override
    public int size() {
        return 0;
    }

    @Override
    public TestPCollection<T> add(T e) {
        return this;
    }

    @Override
    public Iterator<T> iterator() {
        return Collections.<T>emptyList().iterator();
    }
}
