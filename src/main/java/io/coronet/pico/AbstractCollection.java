package io.coronet.pico;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Abstract implementation of the {@code Collection} interface.
 */
public abstract class AbstractCollection
        <E, This extends AbstractCollection<E, This>>
                implements Collection<E> {

    @Override
    public abstract int size();

    @Override
    public boolean contains(Object o) {
        if (o == null) {
            for (Object o2 : this) {
                if (o2 == null) {
                    return true;
                }
            }
        } else {
            for (Object o2 : this) {
                if (o.equals(o2)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean containsAll(java.util.Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public abstract This add(E e);

    @Override
    public This addAll(java.util.Collection<? extends E> c) {
        This result = self();
        for (E e : c) {
            result = result.add(e);
        }
        return result;
    }

    @Override
    public This addAll(Collection<? extends E> c) {
        This result = self();
        for (E e : c) {
            result = result.add(e);
        }
        return result;
    }

    @Override
    public abstract Iterator<E> iterator();

    @Override
    public java.util.Collection<E> asJavaCollection() {
        return new CollectionAdapter<>(this);
    }

    @Override
    public String toString() {
        Iterator<E> iter = iterator();
        if (!iter.hasNext()) {
            return "[]";
        }

        StringBuilder builder = new StringBuilder();
        builder.append('[');

        for (;;) {
            E e = iter.next();
            builder.append(e);

            if (!iter.hasNext()) {
                return builder.append(']').toString();
            }

            builder.append(',').append(' ');
        }
    }

    protected This self() {
        @SuppressWarnings("unchecked")
        This self = (This) this;
        return self;
    }

    private static final class CollectionAdapter<E>
            extends java.util.AbstractCollection<E> {

        private final Collection<E> wrapped;

        public CollectionAdapter(Collection<E> wrapped) {
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
