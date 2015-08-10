package io.coronet.pico;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * A persistent map. Similar to a {@link java.util.Map}, but modifications
 * return a new map instead of mutating this one.
 */
public interface PMap<K, V> {

    /**
     * Returns the number of entries in this map.
     *
     * @return the number of entries in this map
     */
    int size();

    /**
     * Checks whether this is the empty map.
     *
     * @return true if this is the empty map, false otherwise
     */
    default boolean isEmpty() {
        return (size() == 0);
    }

    /**
     * Returns true if this map contains a mapping for the specified key.
     *
     * @param key the key to look for
     * @return true if we have a mapping for the given key
     */
    boolean containsKey(Object key);

    /**
     * Retrieves the value associated with the given key, or returns the
     * given default value if no value is associated.
     *
     * @param key the key to search for
     * @param defaultValue the default value to return if no mapping is found
     * @return the associated value, or the default value if no mapping
     */
    V getOrDefault(Object key, V defaultValue);

    /**
     * Retrieves the value associated with the given key.
     *
     * @param key the key to search for
     * @return the associated value, or null if there is no associated value
     */
    default V get(Object key) {
        return getOrDefault(key, null);
    }

    /**
     * Creates a new map with the given mapping added.
     *
     * @param key the key to add a mapping for
     * @param value the value to map it to
     * @return the new map
     * @throws NullPointerException if key is null
     */
    PMap<K, V> put(K key, V value);

    /**
     * Creates a new map with the given mappings added.
     *
     * @param map the mappings to add
     * @return the new map
     * @throws NullPointerException if map is null
     */
    PMap<K, V> putAll(PMap<? extends K, ? extends V> map);

    /**
     * Creates a new map with the given mappings added.
     *
     * @param map the mappings to add
     * @return the new map
     * @throws NullPointerException if map is null or contains a null key
     */
    PMap<K, V> putAll(Map<? extends K, ? extends V> map);

    /**
     * Creates a new map with the mapping for the given key removed.
     *
     * @param key the key to remove
     * @return the new map
     */
    PMap<K, V> remove(Object key);

    /**
     * Returns an iterable view of the entries in this map.
     *
     * @return the set of entries in this map
     */
    Iterable<Entry<K, V>> entrySet();

    /**
     * Returns an iterable view of the keys in this map.
     *
     * @return the set of keys in this map
     */
    default Iterable<K> keySet() {
        return new Iterable<K>() {
            @Override
            public Iterator<K> iterator() {
                return new Iterator<K>() {

                    private final Iterator<Entry<K, V>> iter =
                            entrySet().iterator();

                    @Override
                    public boolean hasNext() {
                        return iter.hasNext();
                    }

                    @Override
                    public K next() {
                        return iter.next().getKey();
                    }
                };
            }
        };
    }

    /**
     * Executes the given action for each key/value pair in this map, passing
     * each entry as the single argument.
     *
     * @param action the action to execute
     */
    default void forEach(Consumer<? super Entry<? super K, ? super V>> action) {
        for (Entry<K, V> entry : entrySet()) {
            action.accept(entry);
        }
    }

    /**
     * Executes the given action for each key/value pair in this map, passing
     * the key and value as two separate arguments.
     *
     * @param action the action to execute
     */
    default void forEach(BiConsumer<? super K, ? super V> action) {
        for (Entry<K, V> entry : entrySet()) {
            action.accept(entry.getKey(), entry.getValue());
        }
    }

    /**
     * A single entry in a map.
     *
     * @param <K> the type of the key
     * @param <V> the type of the value
     */
    public static final class Entry<K, V> {

        private final K key;
        private final V value;

        /**
         * @param key the key
         * @param value the value
         */
        public Entry(K key, V value) {
            if (key == null) {
                throw new NullPointerException("key");
            }
            this.key = key;
            this.value = value;
        }

        /**
         * @return the key
         */
        public K getKey() {
            return key;
        }

        /**
         * @return the value
         */
        public V getValue() {
            return value;
        }

        @Override
        public String toString() {
            return (key + "=" + value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, value);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Entry<?, ?>)) {
                return false;
            }

            Entry<?, ?> that = (Entry<?, ?>) obj;

            return Objects.equals(this.key, that.key)
                    && Objects.equals(this.value, that.value);
        }
    }
}
