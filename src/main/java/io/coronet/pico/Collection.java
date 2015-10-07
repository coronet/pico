package io.coronet.pico;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A persistent collection - an immutable version of
 * {@link java.util.Collection} whose mutator methods return a new collection
 * rather than modifying this one.
 */
public interface Collection<E> extends Iterable<E> {

    /**
     * Checks if this collection is empty. The default implementation checks
     * whether the size of this collection is zero.
     *
     * @return true if this collection is empty, false otherwise
     * @see java.util.Collection#isEmpty()
     */
    default boolean isEmpty() {
        return (size() == 0);
    }

    /**
     * Returns the size of this collection.
     *
     * @return the number of elements in this collection
     * @see java.util.Collection#size()
     */
    int size();

    /**
     * Checks if this collection contains the given element.
     *
     * @param o the object to look for
     * @return true if this collection contains the object
     * @see java.util.Collection#contains(Object)
     */
    boolean contains(Object o);

    /**
     * Checks if this collection contains <em>all</em> of the given elements.
     *
     * @param c the collection of elements
     * @return true if this collection contains all elements, false otherwise
     * @throws NullPointerException if {@code c} is null
     * @see java.util.Collection#containsAll(java.util.Collection)
     */
    boolean containsAll(java.util.Collection<?> c);

    /**
     * Checks if this collection contains <em>all</em> of the given elements.
     *
     * @param c the collection of elements
     * @return true if this collection contains all elements, false otherwise
     * @throws NullPointerException if {@code c} is null
     * @see java.util.Collection#containsAll(java.util.Collection)
     */
    boolean containsAll(Collection<?> c);

    /**
     * "Adds" an element to this collection, returning a new collection
     * containing all the elements of this collection and the additional
     * element.
     * <p>
     * Depending on the semantics of the collection, adding an element may be
     * a no-op, in which case this method can and should return the object
     * you called it on.
     *
     * @param e the element to add
     * @return a new collection containing the new element
     * @see java.util.Collection#add(Object)
     */
    Collection<E> add(E e);

    /**
     * "Adds" all of the given elements to this collection, returning a new
     * collection containing all the elements from both collection and the
     * other.
     *
     * @param c the collection of elements to add
     * @return the union of this collection and the given collection of elements
     * @see java.util.Collection#addAll(java.util.Collection)
     */
    Collection<E> addAll(java.util.Collection<? extends E> c);

    /**
     * "Adds" all of the given elements to this collection, returning a new
     * collection containing all the elements from both collection and the
     * other.
     *
     * @param c the collection of elements to add
     * @return the union of this collection and the given collection of elements
     * @see java.util.Collection#addAll(java.util.Collection)
     */
    Collection<E> addAll(Collection<? extends E> c);

    /**
     * Returns a view of this collection as an immutable instance of the
     * corresponding standard Java collection type.
     *
     * @return an immutable {@code java.util.Collection}
     */
    java.util.Collection<E> asJavaCollection();

    /**
     * {@inheritDoc}
     * <p>
     * The {@code Spliterator} will be both {@linkplain Spliterator#SIZED sized}
     * and {@linkplain Spliterator#IMMUTABLE immutable}.
     *
     * @see java.util.Collection#spliterator()
     */
    @Override
    default Spliterator<E> spliterator() {
        return Spliterators.spliterator(
                iterator(),
                size(),
                Spliterator.IMMUTABLE);
    }

    /**
     * Returns a sequential {@code Stream} with this collection as its source.
     *
     * @return a sequential stream
     * @see java.util.Collection#stream()
     */
    default Stream<E> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    /**
     * Returns a possibly parallel {@code Stream} with this collection as its
     * source.
     *
     * @return a possibly parallel stream
     * @see java.util.Collection#parallelStream()
     */
    default Stream<E> parallelStream() {
        return StreamSupport.stream(spliterator(), true);
    }
}
