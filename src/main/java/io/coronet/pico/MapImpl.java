package io.coronet.pico;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An implementation of the {@code Map} interface based on a Hash Array Mapped
 * Trie. Heavily influenced by Clojure's {@code PersistentHashMap}.
 * <p>
 * To avoid having to copy the entire index on modification, it is stored as
 * a trie of the <em>hash</em> of the key elements. With 32 slots in each trie
 * node and a 32-bit hash, the trie has a maximum depth of 7 nodes, yielding
 * effectively-constant access time. On modification, only nodes on the path
 * from the root to the leaf of interest need to be copied (again, effectively
 * constant time).
 * <p>
 * Sparsely-populated nodes are stored as a small array indexed by a 32-bit
 * bitmap where the Nth bit indicates the presence of a child node at the given
 * virtual index. The physical index in the indexed array can be determined by
 * counting the number of lower-order bits in the bitmap that are set. More
 * fully-populated nodes are stored in flat, 32-element arrays, since the
 * space efficiency is no longer worthwhile. Special {@code HashCollisionNode}
 * leaves are used to deal with hash collisions - colliding entries are
 * linearly scanned to find the actually-matching element.
 */
final class MapImpl<K, V> extends AbstractMap<K, V, MapImpl<K, V>>
        implements Map<K, V> {

    private static final MapImpl<Object, Object> EMPTY = new MapImpl<>(0, null);

    private static final Object NOT_FOUND = new Object();

    /**
     * @return the empty map
     */
    public static <K, V> MapImpl<K, V> empty() {
        @SuppressWarnings("unchecked")
        MapImpl<K, V> cast = (MapImpl<K, V>) EMPTY;
        return cast;
    }

    private final int size;
    private final Node<K, V> root;

    private MapImpl(int size, Node<K, V> root) {
        this.size = size;
        this.root = root;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean containsKey(Object key) {
        if (key == null) {
            throw new NullPointerException("key");
        }
        if (root == null) {
            return false;
        }

        @SuppressWarnings("unchecked")
        V notFound = (V) NOT_FOUND;

        return (root.get(key.hashCode(), 0, key, notFound) != NOT_FOUND);
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        if (key == null) {
            throw new NullPointerException("key");
        }
        if (root == null) {
            return defaultValue;
        }

        return root.get(key.hashCode(), 0, key, defaultValue);
    }

    /**
     * Creates a new map with the given mapping added.
     *
     * @param key the key to add a mapping for
     * @param value the value to map it to
     * @return the new map
     */
    @Override
    public MapImpl<K, V> put(K key, V value) {
        if (key == null) {
            throw new NullPointerException("key");
        }

        Node<K, V> localRoot = root;
        if (localRoot == null) {
            localRoot = SparseNode.empty();
        }

        PutResult<K, V> result = localRoot.put(
                key.hashCode(),
                0,
                new Entry<>(key, value));

        if (localRoot == result.root) {
            return this;
        }

        int newSize = size;
        if (result.added) {
            newSize += 1;
        }

        return new MapImpl<>(newSize, result.root);
    }

    @Override
    public MapImpl<K, V> remove(Object key) {
        if (key == null) {
            throw new NullPointerException("key");
        }

        if (root == null) {
            // We're already the empty map.
            return this;
        }

        Node<K, V> newRoot = root.remove(key.hashCode(), 0, key);

        if (root == newRoot) {
            // No change.
            return this;
        }

        return new MapImpl<>(size - 1, newRoot);
    }

    @Override
    public Iterable<Entry<K, V>> entrySet() {
        if (size == 0) {
            return Collections.<Entry<K, V>>emptySet();
        }

        return new Iterable<Entry<K, V>>() {
            @Override
            public Iterator<Entry<K, V>> iterator() {
                return root.iterator();
            }
        };
    }

    /**
     * A node in the HAMT.
     */
    private static interface Node<K, V> extends Iterable<Entry<K, V>> {

        /**
         * Gets an element from this node.
         *
         * @param hash the hash code of the key
         * @param level the current level in the trie
         * @param key the full key object
         * @param defaultValue the default value to return if not found
         * @return the associated value, or the default value if not set
         */
        V get(int hash, int level, Object key, V defaultValue);

        /**
         * Puts an element into this node.
         *
         * @param hash the hash code of the key
         * @param level the current level in the trie
         * @param key the full key object
         * @param value the value to associate with the key
         * @return a copy of this node with the given value set
         */
        PutResult<K, V> put(int hash, int level, Entry<K, V> entry);

        /**
         * Removes an element from this node.
         *
         * @param hash the hash code of the key
         * @param level the current level in the trie
         * @param key the full key object
         * @return a copy of this node with the given value removed
         */
        Node<K, V> remove(int hash, int level, Object key);
    }

    /**
     * Shared base class of {@code SparseNode} and {@code FullNode}.
     *
     * TODO: Convert me to a concrete class that contains a mapping
     *       strategy instead of overriding protected methods? May or may not
     *       be worth the extra allocation.
     */
    private static abstract class AbstractNode<K, V> implements Node<K, V> {

        @Override
        public final V get(int hash, int level, Object key, V defaultValue) {
            // Get the next step in the trie based on the hash.
            int index = sliceHashBits(hash, level);
            Object e = get(index);

            if (e == null) {

                // Nothing with this hash prefix in the trie at all.
                return defaultValue;

            } else if (e.getClass() == Entry.class) {

                // Found a key/value pair. Check if the key matches.
                @SuppressWarnings("unchecked")
                Entry<K, V> entry = (Entry<K, V>) e;

                if (key.equals(entry.getKey())) {
                    return entry.getValue();
                } else {
                    return defaultValue;
                }

            } else {

                // It's another node; recurse in.
                @SuppressWarnings("unchecked")
                Node<K, V> node = (Node<K, V>) e;
                return node.get(hash, level + 5, key, defaultValue);

            }
        }

        @Override
        public final PutResult<K, V> put(
                int hash,
                int level,
                Entry<K, V> entry) {

            int index = sliceHashBits(hash, level);
            Object e = get(index);

            if (e == null) {

                // We don't have anything with this hash prefix yet. Insert
                // a new element.
                Node<K, V> node = insert(hash, level, index, entry);
                return new PutResult<>(node, true);

            } else if (e.getClass() == Entry.class) {

                // We've got a single entry with a matching hash prefix;
                // replace it (either directly or with a new node containing
                // both it and the new entry).

                @SuppressWarnings("unchecked")
                Entry<K, V> existing = (Entry<K, V>) e;
                return replace(index, level, existing, hash, entry);

            } else {

                // We've already got an aggregate node at this level; recurse.

                @SuppressWarnings("unchecked")
                Node<K, V> node = (Node<K, V>) e;
                PutResult<K, V> result = node.put(hash, level + 5, entry);

                if (result.root == node) {
                    // Nothing changed, don't bother copying.
                    result.root = this;
                } else {
                    result.root = set(index, result.root);
                }

                return result;

            }
        }

        /**
         * "Replaces" a single entry in the trie with a newer version or a new
         * node containing both old and new entries. Returns a copy of this
         * node with the replacement made.
         *
         * @param index the index of the entry in this node
         * @param level the current level in the trie
         * @param oldEntry the old entry to replace
         * @param newHash the hash of the new entry's key
         * @param newEntry the new entry to replace it with
         * @return a copy of this node with the replacement made
         */
        private PutResult<K, V> replace(
                int index,
                int level,
                Entry<K, V> oldEntry,
                int newHash,
                Entry<K, V> newEntry) {

            if (oldEntry.getKey().equals(newEntry.getKey())) {

                if (oldEntry.getValue() == newEntry.getValue()) {
                    // The new entry is fully equivalent, don't bother.
                    return new PutResult<>(this, false);
                }

                // Replace the old entry with the new entry.
                Node<K, V> node = set(index, newEntry);
                return new PutResult<>(node, false);

            } else {

                // Replace the entry with an aggregate node.
                Node<K, V> newNode = createNode(
                        level + 5,
                        oldEntry,
                        newHash,
                        newEntry);

                Node<K, V> node = set(index, newNode);
                return new PutResult<>(node, true);

            }
        }

        @Override
        public Node<K, V> remove(int hash, int level, Object key) {

            int index = sliceHashBits(hash, level);
            Object e = get(index);

            if (e == null) {

                // Nothing matching that hash prefix.
                return this;

            } else if (e.getClass() == Entry.class) {

                // It's a single key/value pair.
                @SuppressWarnings("unchecked")
                Entry<K, V> entry = (Entry<K, V>) e;

                if (key.equals(entry.getKey())) {
                    // Remove it.
                    return remove(index);
                } else {
                    // Nothing to remove, we're fine.
                    return this;
                }

            } else {

                // It's a node; recurse.
                @SuppressWarnings("unchecked")
                Node<K, V> node = (Node<K, V>) e;

                Node<K, V> result = node.remove(hash, level + 5, key);
                if (result == node) {

                    // No change made.
                    return this;

                } else if (result == null) {

                    // Removed the whole subtree.
                    return remove(index);

                } else {

                    // Removed something but not everything.
                    return set(index, result);

                }

            }
        }

        /**
         * Gets the object at the given (virtual) index in this node.
         *
         * @param index an index between 0 and 31 (inclusive)
         * @return the object at the given index, possibly null
         */
        protected abstract Object get(int index);

        /**
         * "Sets" the value at the given (virtual) index, returning a new
         * node with the value set.
         *
         * @param index the index in this node to set
         * @param value the new value
         * @return a copy of this node with the new value set
         */
        protected abstract Node<K, V> set(int index, Object value);

        /**
         * "Inserts" a new entry into this node at the given (virtual) index,
         * returning a new node with the extra entry included. May inflate
         * the node from sparse to full if need be.
         *
         * @param hash the hash of the key
         * @param level the current level in the trie
         * @param index the index in this node to insert the entry
         * @param entry the entry to insert
         * @return a copy of this node with the new entry inserted
         */
        protected abstract Node<K, V> insert(
                int hash,
                int level,
                int index,
                Entry<K, V> entry);

        /**
         * "Removes" the entry at the given (virtual) index in this node,
         * returning a new node with the given entry removed. May shrink the
         * node from full to sparse, or from sparse to {@code null}.
         *
         * @param index the index of the entry to remove
         * @return a copy of this node with the given element removed
         */
        protected abstract Node<K, V> remove(int index);
    }

    private static abstract class AbstractNodeIterator<K, V>
            implements Iterator<Entry<K, V>> {

        private Iterator<Entry<K, V>> sub;
        private Entry<K, V> next;

        @Override
        public boolean hasNext() {
            if (next != null) {
                return true;
            }

            next = next0();
            return (next != null);
        }

        @Override
        public Entry<K, V> next() {
            Entry<K, V> result = next;
            if (result != null) {
                next = null;
                return result;
            }

            result = next0();
            if (result == null) {
                throw new NoSuchElementException();
            }

            return result;
        }

        private Entry<K, V> next0() {
            if (sub != null) {
                if (sub.hasNext()) {
                    return sub.next();
                } else {
                    sub = null;
                }
            }

            Object result = next1();
            if (result == null) {

                return null;

            } else if (result instanceof Entry<?, ?>) {

                @SuppressWarnings("unchecked")
                Entry<K, V> erased = (Entry<K, V>) result;
                return erased;

            } else {

                @SuppressWarnings("unchecked")
                Node<K, V> erased = (Node<K, V>) result;
                sub = erased.iterator();
                return sub.next();

            }
        }

        protected abstract Object next1();
    }

    /**
     * A node stored in a sparse array indexed by a bitmap. Used when a node has
     * fewer than 16 elements in it; when it grows more full, an array node is
     * more efficient.
     */
    private static final class SparseNode<K, V> extends AbstractNode<K, V> {

        private static final SparseNode<Object, Object> EMPTY_NODE =
                new SparseNode<>(0, new Object[0]);

        /**
         * @return an empty indexed node
         */
        public static <K, V> SparseNode<K, V> empty() {
            @SuppressWarnings("unchecked")
            SparseNode<K, V> cast = (SparseNode<K, V>) EMPTY_NODE;
            return cast;
        }

        private final int bitmap;
        private final Object[] array;

        /**
         * @param bitmap the bitmap for this node
         * @param array the indexed array for this node
         */
        public SparseNode(int bitmap, Object[] array) {
            this.bitmap = bitmap;
            this.array = array;
        }

        @Override
        public Iterator<Entry<K, V>> iterator() {
            return new AbstractNodeIterator<K, V>() {

                private int index;

                @Override
                protected Object next1() {
                    if (index >= array.length) {
                        return null;
                    }

                    Object result = array[index];
                    index += 1;

                    return result;
                }
            };
        }

        @Override
        protected Object get(int index) {
            int bit = (1 << index);
            if ((bitmap & bit) == 0) {
                return null;
            }

            int physicalIndex = getPhysicalIndex(bit);
            return array[physicalIndex];
        }

        @Override
        protected Node<K, V> set(int index, Object value) {
            int bit = (1 << index);
            int physicalIndex = getPhysicalIndex(bit);

            Object[] newArray = array.clone();
            newArray[physicalIndex] = value;

            return new SparseNode<>(bitmap, newArray);
        }

        @Override
        protected Node<K, V> insert(
                int hash,
                int level,
                int index,
                Entry<K, V> entry) {

            if (array.length >= 16) {
                // Inflate to a full node before inserting.
                return inflateAndInsert(hash, level, entry);
            }

            // Stick with a sparse node, just slightly less sparse.
            int bit = (1 << index);
            int physicalIndex = getPhysicalIndex(bit);
            Object[] newArray = cloneAndInsert(array, physicalIndex, entry);

            return new SparseNode<>(bitmap | bit, newArray);
        }

        /**
         * Inflates this sparse node to a full node and inserts the given entry.
         *
         * @param hash the hash code of the key
         * @param level the current level in the trie
         * @param entry the entry being added
         * @return a copy of this node inflated and with the new entry added
         */
        private Node<K, V> inflateAndInsert(
                int hash,
                int level,
                Entry<K, V> entry) {

            Object[] newArray = new Object[32];

            // Add the new node at the appropriate place.
            int idx = sliceHashBits(hash, level);
            newArray[idx] = entry;

            // Add any existing elements at the right place.
            for (int i = 0, j = 0; i < 32; ++i) {
                if (((bitmap >>> i) & 1) != 0) {
                    newArray[i] = array[j];
                    j += 1;
                }
            }

            return new FullNode<>(newArray, array.length);
        }

        @Override
        protected Node<K, V> remove(int index) {
            if (array.length == 1) {
                // No more children, remove me entirely.
                return null;
            }

            int bit = (1 << index);
            int physicalIndex = getPhysicalIndex(bit);

            Object[] newArray = cloneAndRemove(array, physicalIndex);
            return new SparseNode<>(bitmap ^ bit, newArray);
        }

        /**
         * Return the number of bits in the bitmap less than or equal to the
         * given bit; this is the physical index of the corresponding entry in
         * the array.
         *
         * @param bit the bit to search for
         * @return the corresponding index
         */
        private int getPhysicalIndex(int bit) {
            return Integer.bitCount(bitmap & (bit - 1));
        }
    }

    /**
     * When a node grows large enough, store it as a fixed-size 32-element
     * array and directly index it using the appropriate 5-bit
     * slice of the hash.
     */
    private static final class FullNode<K, V> extends AbstractNode<K, V> {

        private final Object[] array;
        private final int count;

        /**
         * @param array the array to wrap
         * @param count the number of non-null entries in the array
         */
        public FullNode(Object[] array, int count) {
            this.array = array;
            this.count = count;
        }

        @Override
        public Iterator<Entry<K,V>> iterator() {
            return new AbstractNodeIterator<K, V>() {

                private int index;

                @Override
                protected Object next1() {
                    while (index < 32) {
                        int i = index++;
                        if (array[i] != null) {
                            return array[i];
                        }
                    }

                    return null;
                }
            };
        }

        @Override
        protected Object get(int index) {
            return array[index];
        }

        @Override
        protected Node<K, V> set(int index, Object value) {
            Object[] newArray = array.clone();
            newArray[index] = value;

            return new FullNode<>(newArray, count);
        }

        @Override
        protected Node<K, V> insert(
                int hash,
                int level,
                int index,
                Entry<K, V> entry) {

            // We're already inflated, just set the appropriate element and
            // update the count.

            Object[] newArray = array.clone();
            newArray[index] = entry;

            return new FullNode<>(newArray, count + 1);
        }

        @Override
        protected Node<K, V> remove(int index) {
            if (count <= 8) {
                // Shrink back to a sparse node.
                return shrinkAndRemove(index);
            }

            // Stick with a full node, just slightly less full.
            Object[] newArray = array.clone();
            newArray[index] = null;

            return new FullNode<>(newArray, count - 1);
        }

        /**
         * Shrinks this node into a sparse node, removing the entry at the
         * given index while we're at it.
         *
         * @param index the index of the entry to remove
         * @return a copy of this node, shrunk, and with the element removed
         */
        private Node<K, V> shrinkAndRemove(int index) {
            Object[] newArray = new Object[count - 1];

            int j = 1;
            int bitmap = 0;

            // Calculate the new bitmap and fill in the packed array, skipping
            // the node at the given index.

            for (int i = 0; i < index; ++i) {
                if (array[i] != null) {
                    newArray[j] = array[i];
                    bitmap |= (1 << i);
                    j += 1;
                }
            }

            for (int i = index + 1; i < array.length; ++i) {
                if (array[i] != null) {
                    newArray[j] = array[i];
                    bitmap |= (1 << i);
                    j += 1;
                }
            }

            return new SparseNode<>(bitmap, newArray);
        }
    }

    /**
     * A leaf {@code Node} representing multiple mappings whose keys have the
     * same hash code. All methods do a linear scan of the known keys with
     * this hash code - not particularly efficient, but collisions should be
     * rare.
     */
    private static final class HashCollisionNode<K, V> implements Node<K, V> {

        private final int hash;
        private final Entry<K, V>[] array;

        /**
         * @param hash the hash of all the keys in this node
         * @param array the array of key/value pairs
         */
        public HashCollisionNode(int hash, Entry<K, V>[] array) {
            this.hash = hash;
            this.array = array;
        }

        @Override
        public V get(int hash, int level, Object key, V defaultValue) {

            // If the hash matches, do a linear scan for a matching key.
            if (hash == this.hash) {
                for (int i = 0; i < array.length; ++i) {
                    Entry<K, V> entry = array[i];
                    if (key.equals(entry.getKey())) {
                        return entry.getValue();
                    }
                }
            }

            // No matching key found.
            return defaultValue;
        }

        @Override
        public PutResult<K, V> put(int hash, int level, Entry<K, V> entry) {

            if (hash != this.hash) {

                // Hash doesn't match; wrap this node in a sparse node, then
                // add the new mapping to it.

                int index = sliceHashBits(this.hash, level);
                int bitmap = 1 << index;
                Object[] newArray = new Object[] { this };

                Node<K, V> newNode = new SparseNode<>(bitmap, newArray);
                return newNode.put(hash, level, entry);

            }

            // If it's one of the keys we have already, replace it.
            for (int i = 0; i < array.length; ++i) {
                Entry<K, V> existing = array[i];
                if (entry.getKey().equals(existing.getKey())) {
                    if (entry.getValue() == existing.getValue()) {
                        return new PutResult<>(this, false);
                    }

                    Entry<K, V>[] newArray = array.clone();
                    newArray[i] = entry;

                    Node<K, V> node = new HashCollisionNode<>(hash, newArray);
                    return new PutResult<>(node, false);
                }
            }

            // Otherwise new key: extend the array.
            Entry<K, V>[] newArray = cloneAndAppend(array, entry);

            Node<K, V> node = new HashCollisionNode<>(hash, newArray);
            return new PutResult<>(node, true);
        }

        @Override
        public Node<K, V> remove(int hash, int level, Object key) {

            // Look for the key and remove it if found.
            if (hash == this.hash) {
                for (int i = 0; i < array.length; ++i) {
                    Entry<K, V> existing = array[i];
                    if (key.equals(existing.getKey())) {

                        if (array.length == 1) {
                            // Node will be empty after we remove this entry.
                            return null;
                        }

                        Entry<K, V>[] newArray = cloneAndRemove(array, i);
                        return new HashCollisionNode<>(hash, newArray);

                    }
                }
            }

            // Key not found; retain existing value(s).
            return this;
        }

        @Override
        public Iterator<Entry<K, V>> iterator() {
            return Arrays.asList(array).iterator();
        }
    }

    private static class PutResult<K, V> {

        public final boolean added;
        public Node<K, V> root;

        public PutResult(Node<K, V> root, boolean added) {
            this.added = added;
            this.root = root;
        }
    }

    /**
     * Slices out the appropriate bits from the given hash for this level of
     * the trie.
     *
     * @param hash the hash code of a key
     * @param level the current level in the trie
     * @return the sliced bits (in the range [0, 31])
     */
    private static int sliceHashBits(int hash, int level) {
        return (hash >>> level) & 0x1F;
    }

    /**
     * Clones the given array, appending the given value. Maybe slightly more
     * efficient than {@code cloneAndInsert} under the same circumstances?
     *
     * @param array the array to clone
     * @param value the value to append
     * @return a copy of the array with the value appended
     */
    private static <T> T[] cloneAndAppend(T[] array, T value) {
        T[] clone = Arrays.copyOf(array, array.length + 1);
        clone[array.length] = value;
        return clone;
    }

    /**
     * Clones the given array, inserting an element at the given index.
     *
     * @param array the array to clone
     * @param index the index at which to insert
     * @param value the value to insert
     * @return a copy of the array with the inserted value
     */
    private static Object[] cloneAndInsert(
            Object[] array,
            int index,
            Object value) {

        Object[] clone = new Object[array.length + 1];

        System.arraycopy(array, 0, clone, 0, index);
        clone[index] = value;
        System.arraycopy(array, index, clone, index + 1, array.length - index);

        return clone;
    }

    /**
     * Clones the given array, removing a single element.
     *
     * @param array the array to clone
     * @param index the index of the element to remove
     * @return the cloned array
     */
    private static <T> T[] cloneAndRemove(T[] array, int index) {
        T[] clone = Arrays.copyOf(array, array.length - 1);
        System.arraycopy(array, index, clone, index, clone.length - index);
        return clone;
    }

    /**
     * Creates a new node containing the given two entries. If their keys have
     * the same hash, it'll be a hash collision node; otherwise it's a sparse
     * node.
     *
     * @param level the current level in the trie
     * @param existingEntry the existing entry
     * @param newHash the hash of the new entry's key
     * @param newEntry the new entry
     * @return a new node containing the two entries
     */
    private static <K, V> Node<K, V> createNode(
            int level,
            Entry<K, V> existingEntry,
            int newHash,
            Entry<K, V> newEntry) {

        int existingHash = existingEntry.getKey().hashCode();
        if (existingHash == newHash) {

            // Hash collision!
            @SuppressWarnings("unchecked")
            Entry<K, V>[] newArray = new Entry[] {
                    existingEntry,
                    newEntry };

            return new HashCollisionNode<>(newHash, newArray);

        } else {

            // Different hashes; create a new sparse node.
            // TODO: Seems like there should be a more efficient way to do this?
            return SparseNode.<K, V>empty()
                    .put(existingHash, level, existingEntry)
                    .root
                    .put(newHash, level, newEntry)
                    .root;

        }
    }
}
