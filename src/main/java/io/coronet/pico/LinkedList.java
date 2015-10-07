package io.coronet.pico;

/**
 * A persistent list that supports "adding" at the head. Suitable for use as a
 * stack.
 */
public interface LinkedList<E> extends List<E> {

    /**
     * Peeks at the top of the stack (aka the first element of the list).
     *
     * @return the element on the top of the stack
     * @throws IndexOutOfBoundsException if the stack is empty
     * @see #first()
     */
    default E peek() {
        return first();
    }

    @Override
    LinkedList<E> first(int n);

    @Override
    LinkedList<E> last(int n);

    /**
     * "Pushes" an element onto the stack, returning a new stack with the new
     * element pushed on top (aka at the beginning of the list).
     *
     * @param e the element to push
     * @return a new stack with the element pushed on top
     * @see #add(Object)
     */
    default LinkedList<E> push(E e) {
        return add(e);
    }

    /**
     * Adds an element to the beginning of this list (aka the top of the stack),
     * returning a new stack with the element prepended.
     */
    @Override
    LinkedList<E> add(E e);

    /**
     * "Adds" the given elements, in order, to the beginning of this list
     * (aka the top of this stack), returning a new stack with the elements
     * prepended. The first element returned by the collection's iterator
     * will end up as the first element of the returned list/the top element
     * on the stack.
     */
    @Override
    LinkedList<E> addAll(java.util.Collection<? extends E> c);

    /**
     * "Adds" the given elements, in order, to the beginning of this list
     * (aka the top of this stack), returning a new stack with the elements
     * prepended. The first element returned by the collection's iterator
     * will end up as the first element of the returned list/the top element
     * on the stack.
     */
    @Override
    LinkedList<E> addAll(Collection<? extends E> c);

    @Override
    LinkedList<E> set(int index, E e);

    /**
     * "Pops" an element off of this stack, returning a new stack with the
     * top element removed.
     *
     * @return a new stack with the top element removed
     * @throws IndexOutOfBoundsException if the stack is empty
     * @see #remove()
     */
    default LinkedList<E> pop() {
        return remove();
    }

    @Override
    LinkedList<E> remove();

    @Override
    LinkedList<E> remove(int n);
}
