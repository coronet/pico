package io.coronet.pico;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * An abstract implementation of the {@code PList} interface.
 *
 * @see java.util.AbstractList
 */
public abstract class AbstractPList<E, This extends AbstractPList<E, This>>
        extends AbstractPCollection<E, This>
        implements PList<E> {

    @Override
    public abstract int size();

    @Override
    public int indexOf(Object o) {
        return indexOf(o, 0, size(), 1);
    }

    @Override
    public int lastIndexOf(Object o) {
        return indexOf(o, size() - 1, -1, -1);
    }

    private int indexOf(Object o, int first, int last, int step) {
        if (o == null) {
            for (int i = first; i != last; i += step) {
                if (get(i) == null) {
                    return i;
                }
            }
        } else {
            for (int i = first; i != last; i += step) {
                if (o.equals(get(i))) {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public abstract E get(int index);

    @Override
    public abstract This first(int n);

    @Override
    public abstract This last(int n);

    @Override
    public abstract This add(E e);

    @Override
    public abstract This set(int index, E e);

    @Override
    public This remove() {
        return last(size() - 1);
    }

    @Override
    public This remove(int n) {
        return last(size() - n);
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {

            private int index;

            @Override
            public boolean hasNext() {
                return index < size();
            }

            @Override
            public E next() {
                if (index >= size()) {
                    throw new NoSuchElementException();
                }
                return get(index++);
            }
        };
    }

    @Override
    public List<E> asJavaCollection() {
        return new ListAdapter<>(this);
    }

    @Override
    public int hashCode() {
        int hash = 1;
        for (E e : this) {
            hash = (31 * hash) + (e == null ? 0 : e.hashCode());
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PList<?>)) {
            return false;
        }

        PList<?> that = (PList<?>) obj;

        Iterator<?> e1 = this.iterator();
        Iterator<?> e2 = that.iterator();

        while (e1.hasNext() && e2.hasNext()) {
            Object o1 = e1.next();
            Object o2 = e2.next();
            if (o1 == null) {
                if (o2 != null) {
                    return false;
                }
            } else {
                if (!o1.equals(o2)) {
                    return false;
                }
            }
        }

        return !(e1.hasNext() || e2.hasNext());
    }

    private static final class ListAdapter<E> extends AbstractList<E> {

        private final PList<E> wrapped;

        public ListAdapter(PList<E> wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public boolean isEmpty() {
            return wrapped.isEmpty();
        }

        @Override
        public int size() {
            return wrapped.size();
        }

        @Override
        public boolean contains(Object o) {
            return wrapped.contains(o);
        }

        @Override
        public E get(int index) {
            return wrapped.get(index);
        }

        @Override
        public int indexOf(Object o) {
            return wrapped.indexOf(o);
        }

        @Override
        public int lastIndexOf(Object o) {
            return wrapped.lastIndexOf(o);
        }

        @Override
        public Iterator<E> iterator() {
            return wrapped.iterator();
        }

        @Override
        public void forEach(Consumer<? super E> action) {
            wrapped.forEach(action);
        }

        @Override
        public Spliterator<E> spliterator() {
            return wrapped.spliterator();
        }

        @Override
        public Stream<E> stream() {
            return wrapped.stream();
        }

        @Override
        public Stream<E> parallelStream() {
            return wrapped.parallelStream();
        }

        @Override
        public String toString() {
            return wrapped.toString();
        }
    }
}
