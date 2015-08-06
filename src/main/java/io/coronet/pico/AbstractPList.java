package io.coronet.pico;

import java.util.Iterator;

/**
 * An abstract implementation of the {@code PList} interface that provides
 * implementations of {@code toString}, {@code hashCode}, and {@code equals}.
 *
 * @see java.util.AbstractList
 */
public abstract class AbstractPList<E> extends AbstractPCollection<E>
        implements PList<E> {

    @Override
    public int hashCode() {
        int hash = 1;
        for (E e : this) {
            hash = (31 * hash) + (e == null ? 0 : e.hashCode());
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PList<?>)) {
            return false;
        }

        PList<?> that = (PList<?>) obj;

        Iterator<?> e1 = this.iterator();
        Iterator<?> e2 = that.iterator();

        while (e1.hasNext() && e2.hasNext()) {
            Object o1 = e1.next();
            Object o2 = e2.next();
            if (o1 == null) {
                if (o2 != null) {
                    return false;
                }
            } else {
                if (!o1.equals(o2)) {
                    return false;
                }
            }
        }

        return !(e1.hasNext() || e2.hasNext());
    }
}
