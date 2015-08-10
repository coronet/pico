package io.coronet.pico;

import java.util.Collection;

/**
 * A persistent stack: a list that supports "adding" at the head.
 */
public interface PStack<E> extends PList<E> {

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
    PStack<E> first(int n);

    @Override
    PStack<E> last(int n);

    /**
     * "Pushes" an element onto the stack, returning a new stack with the new
     * element pushed on top (aka at the beginning of the list).
     *
     * @param e the element to push
     * @return a new stack with the element pushed on top
     * @see #add(Object)
     */
    default PStack<E> push(E e) {
        return add(e);
    }

    /**
     * Adds an element to the beginning of this list (aka the top of the stack),
     * returning a new stack with the element prepended.
     */
    @Override
    PStack<E> add(E e);

    /**
     * "Adds" the given elements, in order, to the beginning of this list
     * (aka the top of this stack), returning a new stack with the elements
     * prepended. The first element returned by the collection's iterator
     * will end up as the first element of the returned list/the top element
     * on the stack.
     */
    @Override
    PStack<E> addAll(Collection<? extends E> c);

    /**
     * "Adds" the given elements, in order, to the beginning of this list
     * (aka the top of this stack), returning a new stack with the elements
     * prepended. The first element returned by the collection's iterator
     * will end up as the first element of the returned list/the top element
     * on the stack.
     */
    @Override
    PStack<E> addAll(PCollection<? extends E> c);

    @Override
    PStack<E> set(int index, E e);

    /**
     * "Pops" an element off of this stack, returning a new stack with the
     * top element removed.
     *
     * @return a new stack with the top element removed
     * @throws IndexOutOfBoundsException if the stack is empty
     * @see #remove()
     */
    default PStack<E> pop() {
        return remove();
    }

    @Override
    PStack<E> remove();

    @Override
    PStack<E> remove(int n);
}
