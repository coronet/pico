package io.coronet.pico;

import java.util.Iterator;


/**
 *
 */
public class PVector<E> extends AbstractPList<E> {

    private static final PVector<Object> EMPTY =
            new PVector<>(0, 0, null, new Object[0]);

    public static <T> PVector<T> create() {
        @SuppressWarnings("unchecked")
        PVector<T> cast = (PVector<T>) EMPTY;
        return cast;
    }

    private final int size;
    private final int depth;
    private final Object[] root;
    private final Object[] tail;

    public PVector(int size, int depth, Object[] root, Object[] tail) {
        this.size = size;
        this.depth = depth;
        this.root = root;
        this.tail = tail;
    }

    @Override
    public <T> PList<T> empty() {
        return create();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public E get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException();
        }

        Object[] block = getBlock(index);

        @SuppressWarnings("unchecked")
        E e = (E) block[index & 0x1F];
        return e;
    }

    @Override
    public PList<E> add(E e) {
        if (tail.length < 32) {
            Object[] nt = new Object[tail.length + 1];
            System.arraycopy(tail, 0, nt, 0, tail.length);
            nt[tail.length] = e;
            return new PVector<>(size + 1, depth, root, nt);
        }

        Object[] newroot;
        int newdepth = depth;

        if ((size >>> 5) > (1 << depth)) {
            newroot = new Object[32];
            newroot[0] = root;
            newroot[1] = newPath(depth, tail);
        } else {
            newroot = pushTail(depth, root, tail);
        }

        return new PVector<>(size + 1, newdepth, newroot, new Object[] { e });
    }

    @Override
    public PList<E> set(int index, E e) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException();
        }
        if (index == size) {
            return add(e);
        }

        if (index >= getTailOffset()) {
            Object[] nt = tail.clone();
            nt[index & 0x1F] = e;
            return new PVector<>(size, depth, root, nt);
        }

        Object[] nr = set(depth, root, index, e);
        return new PVector<>(size, depth, nr, tail);
    }

    private static Object[] set(
            int depth,
            Object[] block,
            int index,
            Object value) {

        Object[] nb = block.clone();
        if (depth == 0) {
            nb[index & 0x1F] = value;
        } else {
            int subIndex = (index >>> depth) & 0x1F;
            Object[] subBlock = (Object[]) nb[subIndex];
            nb[subIndex] = set(depth - 5, subBlock, index, value);
        }
        return nb;
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {

            private int index;
            private int base;
            private Object[] array;

            {
                if (size > 0) {
                    this.array = getBlock(0);
                }
            }

            @Override
            public boolean hasNext() {
                return index < size();
            }

            @Override
            public E next() {
                if (index - base == 32) {
                    array = getBlock(index);
                    base += 32;
                }

                @SuppressWarnings("unchecked")
                E e = (E) array[index & 0x1F];
                index += 1;
                return e;
            }
        };
    }

    private int getTailOffset() {
        return (size - 1) & (~0x1F);
    }

    private Object[] getBlock(int index) {
        if (index >= getTailOffset()) {
            return tail;
        }

        Object[] block = root;

        for (int i = depth; i > 0; i -= 5) {
            int subIndex = (index >>> i) & 0x1F;
            block = (Object[]) block[subIndex];
        }

        return block;
    }

    private Object[] newPath(int depth, Object[] block) {
        if (depth == 0) {
            return block;
        }

        Object[] na = new Object[32];
        na[0] = newPath(depth - 5, block);
        return na;
    }

    private Object[] pushTail(int depth, Object[] block, Object[] tail) {
        int subIndex = ((size - 1) >>> depth) & 0x1F;

        Object[] na = block.clone();
        Object[] toInsert;

        if (depth == 5) {
            toInsert = block;
        } else {
            Object[] child = (Object[]) na[subIndex];
            if (child == null) {
                toInsert = newPath(depth - 5, tail);
            } else {
                toInsert = pushTail(depth - 5, child, tail);
            }
        }

        na[subIndex] = toInsert;
        return na;
    }
}
