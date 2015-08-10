package io.coronet.pico;

import java.util.Collection;

/**
 * A {@code PList} who promises that its {@link #add(Object)} method will
 * append to the end of the list, making it straightforward to use as a
 * queue.
 */
public interface PQueue<E> extends PList<E> {

    /**
     * Creates an empty queue.
     *
     * @return an empty queue
     */
    public static <E> PQueue<E> empty() {
        return PQueueImpl.<E>empty();
    }

    @Override
    PQueue<E> first(int n);

    @Override
    PQueue<E> last(int n);

    /**
     * "Adds" an element to the end of this list/the tail of this queue,
     * returning a new list with the new element appended.
     */
    @Override
    PQueue<E> add(E e);

    /**
     * "Adds" all of the elements in the given collection to the end of this
     * list, returning a new list with the elements appended. Elements are
     * appended in the order returned by the collection's iterator (so the last
     * element returned by the iterator is the last element in the combined
     * list).
     */
    @Override
    PQueue<E> addAll(Collection<? extends E> c);

    /**
     * "Adds" all of the elements in the given collection to the end of this
     * list, returning a new list with the elements appended. Elements are
     * appended in the order returned by the collection's iterator (so the last
     * element returned by the iterator is the last element in the combined
     * list).
     */
    @Override
    PQueue<E> addAll(PCollection<? extends E> c);

    @Override
    PQueue<E> set(int index, E e);

    @Override
    PQueue<E> remove();

    @Override
    PQueue<E> remove(int n);
}
