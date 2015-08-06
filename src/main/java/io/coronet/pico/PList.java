package io.coronet.pico;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * A persistent list - an immutable version of {@code java.util.List}. Mutator
 * methods return a new list with the given modification made.
 */
public interface PList<E> extends PCollection<E> {

    @Override
    <T> PList<T> clear();

    /**
     * Gets the element at the given index.
     *
     * @param index the index
     * @return the element at the given index
     * @throws IndexOutOfBoundsException if index is out of bounds
     * @see List#get(int)
     */
    E get(int index);

    @Override
    PList<E> add(E e);

    /**
     * Sets the element at the given index, returning a new list with the
     * value at the given index replaced.
     *
     * @param index the index to change
     * @param e the new value for the index
     * @return a new list with the element replaced
     * @throws IndexOutOfBoundsException if index is out of bounds
     * @see List#set(int, Object)
     */
    PList<E> set(int index, E e);

    default int indexOf(Object o) {
        if (o == null) {
            for (int i = 0; i < size(); ++i) {
                if (get(i) == null) {
                    return i;
                }
            }
        } else {
            for (int i = 0; i < size(); ++i) {
                if (o.equals(get(i))) {
                    return i;
                }
            }
        }
        return -1;
    }

    default int lastIndexOf(Object o) {
        if (o == null) {
            for (int i = size() - 1; i >= 0; --i) {
                if (get(i) == null) {
                    return i;
                }
            }
        } else {
            for (int i = size() - 1; i >= 0; --i) {
                if (o.equals(get(i))) {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    default Iterator<E> iterator() {
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
    default PList<E> addAll(Collection<? extends E> c) {
        PList<E> result = this;
        for (E e : c) {
            result = result.add(e);
        }
        return result;
    }

    @Override
    default PList<E> addAll(PCollection<? extends E> c) {
        PList<E> result = this;
        for (E e : c) {
            result = result.add(e);
        }
        return result;
    }

    @Override
    default List<E> asJavaCollection() {
        return new ListAdapter<>(this);
    }
}

final class ListAdapter<E> extends AbstractList<E> {

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
