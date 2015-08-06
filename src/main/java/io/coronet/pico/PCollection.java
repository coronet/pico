package io.coronet.pico;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A persistent collection - an immutable version of
 * {@link java.util.Collection} whose mutator methods return a new collection
 * rather than modifying this one.
 */
public interface PCollection<E> extends Iterable<E> {

    /**
     * Checks if this collection is empty.
     * <p>
     * The default implementation checks whether the size of this collection
     * is zero. Override me if calculating the size of your collection is
     * more expensive than some other method of determining whether it's
     * empty.
     *
     * @return true if this collection is empty, false otherwise
     * @see Collection#isEmpty()
     */
    default boolean isEmpty() {
        return (size() == 0);
    }

    /**
     * Returns the empty instance of this collection type.
     *
     * @return the empty instance of this collection type
     * @see Collection#clear()
     */
    <T> PCollection<T> empty();

    /**
     * Returns the size of this collection.
     *
     * @return the number of elements in this collection
     * @see Collection#size()
     */
    int size();

    /**
     * Checks if this collection contains the given element.
     * <p>
     * The default implementation iterates through the collection looking
     * for a match. Override me if there's a more efficient way to tell if
     * this collection contains a given element.
     *
     * @param o the object to look for
     * @return true if this collection contains the object
     * @see Collection#contains(Object)
     */
    default boolean contains(Object o) {
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

    /**
     * Adds an element to this collection, returning a new collection
     * containing all the elements of this collection and the additional
     * element.
     * <p>
     * Depending on the semantics of the collection, adding an element may be
     * a no-op, in which case this method can and should return this object.
     *
     * @param e the element to add
     * @return a new collection containing the new element
     * @see Collection#add(Object)
     */
    PCollection<E> add(E e);

    /**
     * Adds all of the given elements to this collection, returning a new
     * collection containing all the elements from both collection and the
     * other.
     * <p>
     * This implementation simply calls {@link #add(Object)} in a loop. Override
     * it if there's a more efficient way to add multiple elements to your
     * collection at once (ie without allocating all those intermediate
     * collections).
     *
     * @param c the collection of elements to add
     * @return the union of this collection and the given collection of elements
     * @see Collection#addAll(java.util.Collection)
     */
    default PCollection<E> addAll(Collection<? extends E> c) {
        PCollection<E> result = this;
        for (E e : c) {
            result = result.add(e);
        }
        return result;
    }

    /**
     * Adds all of the given elements to this collection, returning a new
     * collection containing all the elements from both collection and the
     * other.
     * <p>
     * This implementation simply calls {@link #add(Object)} in a loop. Override
     * it if there's a more efficient way to add multiple elements to your
     * collection at once (ie without allocating all those intermediate
     * collections).
     *
     * @param c the collection of elements to add
     * @return the union of this collection and the given collection of elements
     * @see Collection#addAll(java.util.Collection)
     */
    default PCollection<E> addAll(PCollection<? extends E> c) {
        PCollection<E> result = this;
        for (E e : c) {
            result = result.add(e);
        }
        return result;
    }

    /**
     * Returns a sequential {@code Stream} with this collection as its source.
     *
     * @return a sequential stream
     * @see Collection#stream()
     */
    default Stream<E> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    /**
     * Returns a possibly parallel {@code Stream} with this collection as its
     * source.
     *
     * @return a possibly parallel stream
     * @see Collection#parallelStream()
     */
    default Stream<E> parallelStream() {
        return StreamSupport.stream(spliterator(), true);
    }

    default Collection<E> asJavaCollection() {
        return new CollectionAdapter<>(this);
    }
}

final class CollectionAdapter<E> extends AbstractCollection<E> {

    private final PCollection<E> wrapped;

    public CollectionAdapter(PCollection<E> wrapped) {
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
