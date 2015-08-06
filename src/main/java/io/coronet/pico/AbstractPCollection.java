package io.coronet.pico;

import java.util.Iterator;

/**
 * Abstract implementation of the {@code PCollection} interface. Adds
 * a standard implementation of {@link Object#toString()}, since all the
 * other methods with default implementations are implemented on the interface.
 */
public abstract class AbstractPCollection<E> implements PCollection<E> {

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
}
