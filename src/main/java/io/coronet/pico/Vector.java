package io.coronet.pico;

/**
 * A {@code List} who promises that its {@link #add(Object)} method will
 * "append" to the end of the list, as in the standard {@code java.util.List}.
 */
public interface Vector<E> extends List<E> {

    /**
     * Returns the empty vector.
     *
     * @return the empty vector
     */
    public static <E> Vector<E> empty() {
        return VectorImpl.<E>empty();
    }

    @Override
    Vector<E> first(int n);

    @Override
    Vector<E> last(int n);

    /**
     * "Adds" an element to the end of this list/the tail of this queue,
     * returning a new list with the new element appended.
     */
    @Override
    Vector<E> add(E e);

    /**
     * "Adds" all of the elements in the given collection to the end of this
     * list, returning a new list with the elements appended. Elements are
     * appended in the order returned by the collection's iterator (so the last
     * element returned by the iterator is the last element in the combined
     * list).
     */
    @Override
    Vector<E> addAll(java.util.Collection<? extends E> c);

    /**
     * "Adds" all of the elements in the given collection to the end of this
     * list, returning a new list with the elements appended. Elements are
     * appended in the order returned by the collection's iterator (so the last
     * element returned by the iterator is the last element in the combined
     * list).
     */
    @Override
    Vector<E> addAll(Collection<? extends E> c);

    @Override
    Vector<E> set(int index, E e);

    @Override
    Vector<E> remove();

    @Override
    Vector<E> remove(int n);
}
