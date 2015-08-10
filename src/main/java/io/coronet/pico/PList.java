package io.coronet.pico;

import java.util.Collection;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;

/**
 * A persistent list - an immutable version of {@code java.util.List}. Mutator
 * methods return a new list with the given modification made.
 */
public interface PList<E> extends PCollection<E> {

    /**
     * Creates a new list whose {@link #add(Object)} method appends elements
     * to the list.
     *
     * @return a new, empty list that can be appended to
     */
    public static <E> PList<E> appendable() {
        return PQueue.empty();
    }

    /**
     * Gets the index of the first occurrence of the given object in this list,
     * or {@code -1} if the element is not found.
     *
     * @param o the object to search for
     * @return the first index of the object, or -1 if not found
     * @see List#indexOf(Object)
     */
    int indexOf(Object o);

    /**
     * Gets the index of the last occurrence of the given object in this list,
     * or {@code -1} if the element is not found.
     *
     * @param o the object to search for
     * @return the last index of the object, or -1 if not found
     * @see List#lastIndexOf(Object)
     */
    int lastIndexOf(Object o);

    /**
     * Gets the element at the given index.
     *
     * @param index the index
     * @return the element at the given index
     * @throws IndexOutOfBoundsException if index is out of bounds
     * @see List#get(int)
     */
    E get(int index);

    /**
     * Gets the first element in this list.
     *
     * @return the first element in this list
     * @throws IndexOutOfBoundsException if this list is empty
     */
    default E first() {
        return get(0);
    }

    /**
     * Gets the first {@code n} elements in this list.
     *
     * @param n the number of elements to get
     * @return a list containing only the first n elements
     * @throws IndexOutOfBoundsException if {@code n < 0} or {@code n >= size()}
     */
    PList<E> first(int n);

    /**
     * Returns the last element in this list.
     *
     * @return the last element in this list
     * @throws IndexOutOfBoundsException if this list is empty
     */
    default E last() {
        return get(size() - 1);
    }

    /**
     * Returns the last {@code n} elements in this list.
     *
     * @param n the number of elements to get
     * @return a list containing on the last n elements
     * @throws IndexOutOfBoundsException if {@code n < 0} or {@code n >= size()}
     */
    PList<E> last(int n);

    /**
     * Adds an element to this list. The position where the element will be
     * inserted is implementation-dependent. A {@link PStack} inserts elements
     * at the head of the list, a {@link PQueue} inserts elements at the tail.
     */
    @Override
    PList<E> add(E e);

    /**
     * {@inheritDoc}
     * <p>
     * The elements will be added in the order they are returned by the
     * collection's iterator. The position that they will be inserted
     * at is implementation-dependent (typically congruent with the behavior
     * of {@link #add(Object)}).
     */
    @Override
    PList<E> addAll(Collection<? extends E> c);

    /**
     * {@inheritDoc}
     * <p>
     * The elements will be added in the order they are returned by the
     * collection's iterator. The position that they will be inserted
     * at is implementation-dependent (typically congruent with the behavior
     * of {@link #add(Object)}).
     */
    @Override
    PList<E> addAll(PCollection<? extends E> c);

    /**
     * "Sets" the element at the given index, returning a new list with the
     * value at the given index replaced.
     *
     * @param index the index to change
     * @param e the new value for the index
     * @return a new list with the element replaced
     * @throws IndexOutOfBoundsException if index is out of bounds
     * @see List#set(int, Object)
     */
    PList<E> set(int index, E e);

    /**
     * "Removes" a single element from the head of this list, returning a new
     * list containing only the remaining elements. It's precisely equivalent
     * to calling {@code list.last(list.size() - 1)}, just a little more
     * convenient.
     *
     * @return a new list containing the remaining elements
     * @throws IndexOutOfBoundsException if the list is empty
     * @see #last(int)
     */
    PList<E> remove();

    /**
     * "Removes" {@code n} elements from the head of this list, returning a new
     * list containing only the remaining elements. It's precisely equivalent
     * to calling {@code list.last(list.size() - n)}, just a little more
     * convenient.
     *
     * @param n the number of elements to remove
     * @return a new list containing the remaining elements
     * @throws IndexOutOfBoundsException if {@code n < 0} or {@code n >= size()}
     * @see #last(int)
     */
    PList<E> remove(int n);

    @Override
    List<E> asJavaCollection();

    /**
     * {@inheritDoc}
     * <p>
     * The {@code Spliterator} will additionally be
     * {@linkplain Spliterator#ORDERED ordered}.
     */
    @Override
    default Spliterator<E> spliterator() {
        return Spliterators.spliterator(
                iterator(),
                size(),
                Spliterator.IMMUTABLE | Spliterator.ORDERED);
    }
}
