package io.coronet.pico;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A persistent vector. Appends and sets are effectively O(1): theoretically
 * O(log[32] n), but you're going to run out of integers well before you notice.
 * Iteration's cheap as well. Based on Clojure's {@code PersistentVector}, but
 * with an interface that's more familiar to a Java programmer.
 * <p>
 * "Most" elements are stored in order in the leaf nodes of a 32-ary tree, which
 * facilitates quick lookups by index, and persistent 'modifications' without
 * having to copy the whole tree. A small number of elements from the tail of
 * the vector are stored separately to optimize appends - elements are appended
 * to the tail, which is then periodically "flushed" into the tree when it
 * grows large enough.
 */
public class PVector<E> extends AbstractPList<E> {

    private static final PVector<Object> EMPTY =
            new PVector<>(0, 0, null, new Object[0]);

    /**
     * @return the empty vector
     */
    public static <T> PVector<T> empty() {
        @SuppressWarnings("unchecked")
        PVector<T> cast = (PVector<T>) EMPTY;
        return cast;
    }

    // TODO: create methods

    private final int size;
    private final int depth;
    private final Object[] root;
    private final Object[] tail;

    /**
     * @param size the total size of this vector
     * @param depth the depth of the tree
     * @param root the root node of the tree
     * @param tail the tail of the vector
     */
    private PVector(int size, int depth, Object[] root, Object[] tail) {
        this.size = size;
        this.depth = depth;
        this.root = root;
        this.tail = tail;
    }

    @Override
    public <T> PList<T> clear() {
        return empty();
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
            // Easy case - there's room in the tail.
            Object[] nt = new Object[tail.length + 1];
            System.arraycopy(tail, 0, nt, 0, tail.length);
            nt[tail.length] = e;
            return new PVector<>(size + 1, depth, root, nt);
        }

        // Less easy case - the tail is full. Push it into the tree and start
        // a new tail with the single element being added.

        Object[] newroot;
        int newdepth = depth;

        if (root == null) {

            // Very first time we push something into the tree; just copy
            // the tail in to the root.
            newroot = tail;

        } else if ((size >>> 5) > (1 << depth)) {

            // Tree is completely fully, push the root down a level and create a
            // new root containing it and the tail.
            newroot = new Object[2];
            newroot[0] = root;
            newroot[1] = newPathToTail(depth);
            newdepth += 5;

        } else {

            // Tree still has room to grow, push the tail down to the next
            // available spot.
            newroot = pushTail(depth, root);

        }

        return new PVector<>(size + 1, newdepth, newroot, new Object[] { e });
    }

    @Override
    public PList<E> set(int index, E e) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException();
        }

        if (index == size) {
            // Just treat this like an add.
            return add(e);
        }

        if (index >= getTailOffset()) {
            // Easy case; it's in the tail.
            Object[] nt = tail.clone();
            nt[index & 0x1F] = e;
            return new PVector<>(size, depth, root, nt);
        }

        // Slightly harder case - it's in the tree.
        Object[] nr = set(depth, root, index, e);
        return new PVector<>(size, depth, nr, tail);
    }

    /**
     * Recursively modifies an element of the tree by index.
     *
     * @param level the level of the block
     * @param block the block to modify
     * @param index the index of the element to modify
     * @param value the new value of the element
     * @return a copy of the block with the modification applied
     */
    private static Object[] set(
            int level,
            Object[] block,
            int index,
            Object value) {

        Object[] nb = block.clone();

        if (level == 0) {
            // Base case; directly insert the value.
            nb[index & 0x1F] = value;
        } else {
            // Recurse, then swap the result in for the appropriate place in
            // this block.
            int subIndex = getSubIndex(index, level);
            Object[] subBlock = (Object[]) nb[subIndex];
            nb[subIndex] = set(level - 5, subBlock, index, value);
        }

        return nb;
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {

            private int index;
            private int base = -32;
            private Object[] array;

            @Override
            public boolean hasNext() {
                return index < size();
            }

            @Override
            public E next() {
                if (index >= size) {
                    throw new NoSuchElementException();
                }

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

    /**
     * Gets the offset of the first element in the tail.
     *
     * @return the offset of the tail
     */
    private int getTailOffset() {
        return (size - tail.length);
    }

    /**
     * Gets the sub-index within a particular block of the path to the element
     * with the given index. At level 0, this is the low 5 bits of the index;
     * at level 1 it's the next-lowest 5 bits; and so forth.
     *
     * @param index the index of an element
     * @param level the level of the current block
     * @return the sub-index of the path to the element in this block
     */
    private static int getSubIndex(int index, int level) {
        return (index >>> level) & 0x1F;
    }

    /**
     * Gets the leaf block that the element with the given index belongs to.
     *
     * @param index the index of an element
     * @return the leaf block it belongs to
     */
    private Object[] getBlock(int index) {
        // Easy case - it's in the tail.
        if (index >= getTailOffset()) {
            return tail;
        }

        // Slightly harder case - walk down the tree to find the right spot.
        Object[] block = root;
        for (int i = depth; i > 0; i -= 5) {
            int subIndex = getSubIndex(index, i);
            block = (Object[]) block[subIndex];
        }
        return block;
    }

    /**
     * Pushes the tail node into the given block, growing it if need be.
     *
     * @param level the depth of the block
     * @param block the block to insert to
     * @return a copy of the block with the tail pushed in
     */
    private Object[] pushTail(int level, Object[] block) {
        // The index in this block where the path to the tail belongs.
        int subIndex = getSubIndex(size - 1, level);

        // Clone the block, growing it if needed.
        Object[] na;
        if (subIndex == block.length) {
            na = new Object[block.length + 1];
            System.arraycopy(block, 0, na, 0, block.length);
        } else {
            na = block.clone();
        }

        Object[] toInsert;

        if (level == 5) {
            // Base case - directly insert the tail.
            toInsert = tail;
        } else if (subIndex == block.length) {
            // If we're off the right edge of the tree, construct a new path
            // and graft it in (we grew the block above to have a spot).
            toInsert = newPathToTail(level - 5);
        } else {
            // Recurse into the appropriate child block, then swap in the
            // result to the copy of this block.
            Object[] child = (Object[]) na[subIndex];
            toInsert = pushTail(level - 5, child);
        }

        na[subIndex] = toInsert;
        return na;
    }

    /**
     * Creates a new path of the appropriate depth to the tail. Each interior
     * block in the path is a one-element array pointing to the next block,
     * until the final block points to the current tail (which should be
     * full at this point).
     * <p>
     * <code>
     *         ...
     *          /
     *       (block)
     *        /
     *     (block)
     *     /
     * (tail)
     * </code>
     * <p>
     * This path is then welded on to the appropriate place on the tree,
     * constructing a new tree that's slightly more full.
     *
     * @param depth the depth of the path (in increments of 5)
     * @return a path to the current tail node
     */
    private Object[] newPathToTail(int depth) {
        Object[] na = tail;
        for (int i = 0; i < depth; i += 5) {
            Object[] nna = new Object[1];
            nna[0] = na;
            na = nna;
        }
        return na;
    }
}
