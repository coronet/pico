package io.coronet.pico;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A persistent vector - AKA a persistent list that supports efficient adding to
 * the end. The implementation is based on Clojure's {@code PersistentVector}.
 * <p>
 * "Most" elements are stored in order in the leaf nodes of a 32-ary tree, which
 * facilitates quick lookups by index, and persistent 'modifications' without
 * having to copy the whole tree. A small number of elements from the tail of
 * the vector are stored separately to optimize appends - elements are appended
 * to the tail, which is then periodically "flushed" into the tree when it
 * grows large enough.
 */
final class VectorImpl<E> extends AbstractList<E, VectorImpl<E>>
        implements Vector<E> {

    private static final VectorImpl<Object> EMPTY =
            new VectorImpl<>(0, 0, null, 0, new Object[0]);

    /**
     * @return the empty vector
     */
    public static <T> VectorImpl<T> empty() {
        @SuppressWarnings("unchecked")
        VectorImpl<T> cast = (VectorImpl<T>) EMPTY;
        return cast;
    }

    // TODO: create methods

    private final int offset;
    private final int totalSize;
    private final Object[] treeRoot;
    private final int treeDepth;
    private final Object[] tail;

    /**
     * @param offset the offset of the first element of this vector
     * @param totalSize the total size of this vector
     * @param treeRoot the root node of the tree portion of this vector
     * @param treeDepth the depth of the tree portion of this vector
     * @param tail the tail of the vector
     */
    private VectorImpl(
            int offset,
            int totalSize,
            Object[] treeRoot,
            int treeDepth,
            Object[] tail) {

        this.offset = offset;
        this.totalSize = totalSize;
        this.treeRoot = treeRoot;
        this.treeDepth = treeDepth;
        this.tail = tail;
    }

    @Override
    public int size() {
        // We may have some nulls at the beginning of the tree if elements have
        // been "removed" from the head of the vector; subtract them out from
        // the total size of the data structure to get the user-facing size.
        return (totalSize - offset);
    }

    @Override
    public E get(int index) {
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException();
        }

        // We may have some nulls at the beginning of the tree if elements have
        // been "removed" from the head of the vector; add them in to get the
        // real index in the data structure.

        int realIndex = index + offset;
        Object[] array = getArray(realIndex);

        @SuppressWarnings("unchecked")
        E e = (E) array[realIndex & 0x1F];
        return e;
    }

    /**
     * Gets the array that the element with the given (real) index belongs
     * to. Returns either the tail or the contents of a leaf node of the tree.
     *
     * @param index the real index of an element
     * @return the array it belongs to
     */
    private Object[] getArray(int index) {
        if (index >= getTreeSize(totalSize)) {
            return tail;
        } else {
            return getArray(treeRoot, treeDepth, index);
        }
    }

    @Override
    public VectorImpl<E> first(int n) {
        int size = size();
        if (n < 0 || n > size) {
            throw new IndexOutOfBoundsException();
        }

        if (n == 0) {
            return empty();
        }
        if (n == size) {
            return this;
        }

        // We're carrying over the offset, so figure out the real size of the
        // new data structure.

        int newSize = n + offset;

        if (newSize > getTreeSize(totalSize)) {

            // Easy case - just squish the tail.
            Object[] newTail = Arrays.copyOf(tail, newSize & 0x1F);
            return new VectorImpl<>(
                    offset,
                    newSize,
                    treeRoot,
                    treeDepth,
                    newTail);

        } else {

            // Harder case - prune the tree.
            PruneRightResult result =
                    pruneRight(treeRoot, treeDepth, newSize - 1, true);

            return new VectorImpl<>(
                    offset,
                    newSize,
                    result.root,
                    result.depth,
                    result.tail);

        }
    }

    @Override
    public VectorImpl<E> last(int n) {
        int size = size();
        if (n < 0 || n > size) {
            throw new IndexOutOfBoundsException();
        }

        if (n == 0) {
            return empty();
        }
        if (n == size) {
            return this;
        }

        int newOffset = offset + (size - n);

        if (newOffset >= getTreeSize(totalSize)) {

            // Easy case - just return the corresponding portion of the tail.
            Object[] newTail;
            if (n == tail.length) {
                newTail = tail;
            } else {
                newTail = Arrays.copyOfRange(
                    tail,
                    tail.length - n,
                    tail.length);
            }

            return new VectorImpl<>(0, n, null, 0, newTail);

        } else {

            PruneLeftResult result =
                    pruneLeft(treeRoot, treeDepth, newOffset, true);

            return new VectorImpl<>(
                    result.offset,
                    result.offset + n,
                    result.root,
                    result.depth,
                    tail);
        }
    }

    @Override
    public VectorImpl<E> add(E e) {
        if (totalSize == Integer.MAX_VALUE) {
            // Can't grow the underlying data structure any more.
            throw new OutOfMemoryError();
        }

        if (tail.length < 32) {
            // Easy case - there's room in the tail.
            Object[] newTail = Arrays.copyOf(tail, tail.length + 1);
            newTail[tail.length] = e;

            return new VectorImpl<>(
                    offset,
                    totalSize + 1,
                    treeRoot,
                    treeDepth,
                    newTail);
        }

        // Less easy case - the tail is full. Push it into the tree and start
        // a new tail with the single element being added.

        return pushAndAdd(e);
    }

    /**
     * Pushes the current tail into the tree and starts a new tail with the
     * given element. Called by {@code add()} when the tail is full.
     *
     * @param e the element to add to the new tail
     * @return a copy of this vector with the element appended
     */
    private VectorImpl<E> pushAndAdd(E e) {
        Object[] newRoot;
        int newDepth = treeDepth;

        if (treeRoot == null) {

            // Very first time we push something into the tree; just copy
            // the current tail in as the new root.
            newRoot = tail;

        } else if (isTreeFull()) {

            // Tree is completely full, push the root up a level and create a
            // new root containing the old root and the (appropriately-path'd)
            // tail.
            newRoot = grow(treeRoot, treeDepth, tail);
            newDepth += 5;

        } else {

            // Tree still has room to grow, push the tail down to the next
            // available spot.
            newRoot = append(treeRoot, treeDepth, tail, totalSize - 1);

        }

        return new VectorImpl<>(
                offset,
                totalSize + 1,
                newRoot,
                newDepth,
                new Object[] { e });
    }

    /**
     * Returns whether the tree portion of the data structure is full. If it
     * is, we need to push the tree up a level to make room for inserting any
     * new nodes.
     *
     * @return true if the tree portions if the data structure is full
     */
    private boolean isTreeFull() {
        // The number of leaf nodes required to store the whole vector
        // (each leaf node stores 32 elements).
        int requiredLeafNodes = (totalSize >>> 5);

        // The maximum number of leaf nodes we can have in a tree of this
        // depth (each interior node stores 32 children).
        int maxLeafNodes = (1 << treeDepth);

        return (requiredLeafNodes > maxLeafNodes);
    }

    @Override
    public VectorImpl<E> set(int index, E e) {
        if (index < 0 || index > size()) {
            throw new IndexOutOfBoundsException();
        }

        int realIndex = index + offset;
        if (realIndex == totalSize) {

            // Just treat this like an add.
            return add(e);

        } else if (realIndex >= getTreeSize(totalSize)) {

            // Easy case; it's in the tail.
            Object[] newTail = tail.clone();
            newTail[realIndex & 0x1F] = e;
            return new VectorImpl<>(
                    offset,
                    totalSize,
                    treeRoot,
                    treeDepth,
                    newTail);

        } else {

            // Slightly harder case - it's in the tree.
            Object[] newRoot = set(treeRoot, treeDepth, e, realIndex);
            return new VectorImpl<>(
                    offset,
                    totalSize,
                    newRoot,
                    treeDepth,
                    tail);

        }
    }

    /**
     * Recursively modifies an element of the tree by index.
     *
     * @param root the root of the tree to modify
     * @param depth the depth of the tree
     * @param value the new value of the element
     * @param index the index of the element to modify
     * @return a copy of the tree with the modification applied
     */
    private static Object[] set(
            Object[] root,
            int depth,
            Object value,
            int index) {

        Object[] newRoot = root.clone();

        if (depth == 0) {
            // Base case; directly insert the value.
            newRoot[index & 0x1F] = value;
        } else {
            // Recurse, then swap the result in for the appropriate place in
            // this node.
            int nodeIndex = getNodeIndex(index, depth);
            Object[] child = (Object[]) root[nodeIndex];
            newRoot[nodeIndex] = set(child, depth - 5, value, index);
        }

        return newRoot;
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {

            private int index = offset;
            private Object[] array;

            {
                // Pre-warm the array if we're not starting on an even
                // multiple of 32.
                if ((offset & 0x1F) != 0) {
                    array = getArray(offset);
                }
            }

            @Override
            public boolean hasNext() {
                return index < totalSize;
            }

            @Override
            public E next() {
                if (index >= totalSize) {
                    throw new NoSuchElementException();
                }

                // Roll over to the next array.
                if ((index & 0x1F) == 0) {
                    array = getArray(index);
                }

                @SuppressWarnings("unchecked")
                E e = (E) array[index & 0x1F];
                index += 1;
                return e;
            }
        };
    }

    /**
     * Gets the size of the tree for a vector of a given (total) size. The
     * tree stores elements in blocks of 32, and the tail always stores less
     * than or equal to 32 elements, so we round down from the total size to
     * find the number of elements in the tree.
     *
     * @param totalSize the total size of a data structure
     * @return the size of the tree for a data structure of the given size
     */
    private static int getTreeSize(int totalSize) {
        if (totalSize <= 32) {
            return 0;
        } else {
            // TODO: Clojure does ((realSize - 1) >>> 5) << 5); is that faster?
            return ((totalSize - 1) & (~0x1F));
        }
    }

    /**
     * Gets the index within a particular node of the path to the element
     * with the given index. At depth 0 (the leaf node), this is the low 5 bits
     * of the index, and is the direct index of the element within the leaf
     * node. At the next depth (5) it's bits 5-9 of the index, containing the
     * index of the leaf node, and so forth.
     *
     * @param index the index of an element
     * @param depth the depth of the tree from the given node
     * @return the index of the path to the given element in this node
     */
    private static int getNodeIndex(int index, int depth) {
        return (index >>> depth) & 0x1F;
    }

    /**
     * Gets the array that the element with the given index belongs to. Always
     * returns the contents of a leaf node of the tree; called recursively
     * from the root to traverse the tree to find the appropriate leaf node.
     *
     * @param root the root of a tree
     * @param depth the depth of the tree starting from this node
     * @param index the index of an element to get the array for
     * @return the array the given element belongs in
     */
    private static Object[] getArray(Object[] root, int depth, int index) {
        if (depth == 0) {

            // This is a leaf node, return the array.
            return root;

        } else {

            // This is an internal node; recurse into the appropriate child.
            int nodeIndex = getNodeIndex(index, depth);
            Object[] child = (Object[]) root[nodeIndex];
            return getArray(child, depth - 5, index);

        }
    }

    /**
     * Grows this tree by allocating a new two-element root node, using the
     * existing tree as its leftmost node and a singleton path to the given
     * leaf node as its rightmost node.
     * <p>
     * <code>
     *              (new root)
     *             /         \
     *       (old root)      (node)
     *        / ... \         /
     *     (node)  (node)    (node)
     *     /   |    |  \      /
     *   (L)  (L)  (L) (L)  (leaf)
     * </code>
     *
     * @param root the array for the root node
     * @param depth the depth of the tree
     * @param leaf the leaf node to append
     * @return a new root node containing the old root and the new leaf
     */
    private static Object[] grow(Object[] root, int depth, Object[] leaf) {
        Object[] newArray = new Object[2];
        newArray[0] = root;
        newArray[1] = newPath(depth, leaf);
        return newArray;
    }

    /**
     * Appends the given leaf node into the tree rooted at this node. Assumes
     * the tree has room - the caller is responsible for tracking the size of
     * the tree and {@linkplain #grow(Object[], int, Object[]) growing} it if
     * need be instead of calling this method.
     *
     * @param root the root node of a tree
     * @param depth the depth of the tree
     * @param leaf the leaf node to append
     * @param index the index of the last element in the leaf
     * @return a copy of this tree with the new leaf appended
     */
    private static Object[] append(
            Object[] root,
            int depth,
            Object[] leaf,
            int index) {

        // The index in this node where the path to the new leaf lives. It will
        // be either the last element in the array or one element past the end;
        // this turns out to be the easiest way to tell.
        int nodeIndex = getNodeIndex(index, depth);

        // Clone the array, growing if need be.
        Object[] newArray;
        if (nodeIndex == root.length) {
            newArray = Arrays.copyOf(root, root.length + 1);
        } else {
            newArray = root.clone();
        }

        Object[] toInsert;
        if (depth == 5) {
            // Base case - directly insert the leaf.
            toInsert = leaf;
        } else if (nodeIndex == root.length) {
            // If we're off the right edge of the tree, construct a new path
            // and graft it in.
            toInsert = newPath(depth - 5, leaf);
        } else {
            // Recurse into the appropriate child node.
            Object[] child = (Object[]) root[nodeIndex];
            toInsert = append(child, depth - 5, leaf, index);
        }

        // Set/overwrite the path in the new array
        newArray[nodeIndex] = toInsert;
        return newArray;
    }

    /**
     * Creates a new path of the appropriate depth to a leaf. Each interior
     * node in the path has its 0th element pointing to the next node down,
     * until the final node points to the given leaf node.
     * <p>
     * <code>
     *        ...
     *        /
     *     (node)
     *     /
     * (leaf)
     * </code>
     * <p>
     * This path will then be welded on to the appropriate place on the tree,
     * constructing a new tree that's slightly more full.
     * <p>
     * <code>
     *     \
     *  (existing)
     *    /   \
     *  ...  (path)
     *        /
     *      ...
     * </code>
     *
     * @param length the length of the path (in increments of 5)
     * @param block the block at the
     * @return a path to the current tail node
     */
    private static Object[] newPath(int length, Object[] leaf) {
        Object[] node = leaf;
        for (int i = 0; i < length; i += 5) {
            node = new Object[] { node };
        }
        return node;
    }

    /**
     * Recursively prunes elements from the right side of the tree as part of
     * a call to {@link #first(int)}. Returns a reduced tree and a new tail.
     *
     * @param root the root of the tree
     * @param depth the depth of the tree
     * @param index the (real) index of the last element to keep
     * @param leftEdge true if this node is on the left edge of the tree
     * @return the pruned tree and tail
     */
    private static PruneRightResult pruneRight(
            Object[] root,
            int depth,
            int index,
            boolean leftEdge) {

        if (depth == 0) {
            // Leaf node - chomp it down and it becomes the new tail.
            int newSize = (index & 0x1F) + 1;
            Object[] newTail = Arrays.copyOf(root, newSize);
            return new PruneRightResult(newTail);
        }

        // An interior node. Find the index of the path to the last element
        // we want to keep in this node.

        int nodeIndex = getNodeIndex(index, depth);

        // If we're on the left edge of the tree and the child branch we're
        // pruning is too, then this node is redundant and may be collapsed
        // out, reducing the depth of the resulting tree.

        boolean childOnLeftEdge = leftEdge && (nodeIndex == 0);

        // Recursively prune it.
        Object[] child = (Object[]) root[nodeIndex];
        PruneRightResult result = pruneRight(
                child,
                depth - 5,
                index,
                childOnLeftEdge);

        if (childOnLeftEdge) {
            return result;
        } else {
            return finishPruneRight(root, nodeIndex, result);
        }
    }

    /**
     * Finishes pruning a tree from the right after making a recursive call
     * to {@code pruneRight} on a child.
     *
     * @param root the root of the tree
     * @param nodeIndex the index of the child branch that was pruned
     * @param result the result of pruning the child branch
     * @return the result of pruning the overall tree
     */
    private static PruneRightResult finishPruneRight(
            Object[] root,
            int nodeIndex,
            PruneRightResult result) {

        if (result.root == null) {

            // The child was totally pruned.

            if (nodeIndex == 0) {

                // That was our only child; prune this node too (but we're
                // not on the left edge, so can't totally collapse).
                result.depth += 5;
                return result;

            } else {

                // Only some children pruned, trim the block down.
                result.root = Arrays.copyOf(root, nodeIndex);
                result.depth += 5;
                return result;

            }

        } else {

            // Child was partially pruned, shrink this array and swap in the
            // updated last node.
            Object[] newRoot = Arrays.copyOf(root, nodeIndex + 1);
            newRoot[nodeIndex] = result.root;

            result.root = newRoot;
            result.depth += 5;
            return result;

        }
    }

    /**
     * Recursively prunes elements from the left side of the tree as part of a
     * call to {@link #last(int)}. Returns a reduced tree and the offset of
     * the first non-pruned element in the reduced tree.
     *
     * @param root the root of the tree to prune
     * @param depth the depth of the tree
     * @param index the index of the first element to keep
     * @param rightEdge true if we're on the right edge of the tree
     * @return the pruned tree and offset
     */
    private static PruneLeftResult pruneLeft(
            Object[] root,
            int depth,
            int index,
            boolean rightEdge) {

        if (depth == 0) {

            // Found the leaf node we're interested in. Can't shift it without
            // messing up the tail, so null out the entries we're "removing"
            // and set an appropriate offset.

            int nodeIndex = index & 0x1F;
            if (nodeIndex == 0) {
                // Pruned on a node boundary; don't bother cloning.
                return new PruneLeftResult(root, 0);
            } else {
                // Clone and null out stuff we don't need so it can be GC'd.
                Object[] newRoot = root.clone();
                for (int i = 0; i < nodeIndex; ++i) {
                    newRoot[i] = null;
                }
                return new PruneLeftResult(newRoot, nodeIndex);
            }
        }

        // An interior node. Find the index of the path to the last element
        // we want to keep in this node.

        int nodeIndex = getNodeIndex(index, depth);

        // If we're on the right edge of the tree and the child branch we're
        // pruning is too, then this node is redundant and may be collapsed
        // out, reducing the depth of the resulting tree.

        boolean childOnRightEdge = rightEdge && (nodeIndex == root.length - 1);

        // Recursively prune it.
        Object[] child = (Object[]) root[nodeIndex];
        PruneLeftResult result = pruneLeft(
                child,
                depth - 5,
                index,
                childOnRightEdge);

        if (childOnRightEdge) {
            // Prune this node out entirely.
            return result;
        }

        if (nodeIndex == 0 && result.root == child) {
            // Pruned on a boundary; don't bother cloning.
            result.root = root;
            result.depth += 5;
            return result;
        }

        if (rightEdge) {
            // We can safely shift things over if there's space.
            Object[] newRoot = Arrays.copyOfRange(
                    root,
                    nodeIndex,
                    root.length);

            newRoot[0] = result.root;

            result.root = newRoot;
            result.depth += 5;
            return result;
        }

        // Can't shift, null things out and increase the offset.
        Object[] newRoot = root.clone();
        for (int i = 0; i < nodeIndex; ++i) {
            newRoot[i] = null;
        }
        newRoot[nodeIndex] = result.root;
        result.root = newRoot;
        result.depth += 5;

        result.offset += (nodeIndex * (1 << depth));
        return result;
    }

    /**
     * The result of a call to
     * {@link VectorImpl#pruneRight(Object[], int, int, boolean)}.
     */
    private static final class PruneRightResult {

        public final Object[] tail;
        public Object[] root;
        public int depth;

        public PruneRightResult(Object[] tail) {
            this.tail = tail;
        }
    }

    /**
     * The result of a call to
     * {@link VectorImpl#pruneLeft(Object[], int, int, boolean)}.
     */
    private static final class PruneLeftResult {

        public Object[] root;
        public int depth;
        public int offset;

        public PruneLeftResult(Object[] root, int offset) {
            this.root = root;
            this.offset = offset;
        }
    }
}
