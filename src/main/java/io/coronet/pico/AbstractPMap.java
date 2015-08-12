package io.coronet.pico;

import java.util.Iterator;
import java.util.Map;

/**
 * Abstract implementation of the {@code PMap} interface.
 *
 * @see java.util.AbstractMap
 */
public abstract class AbstractPMap<K, V, This extends AbstractPMap<K, V, This>>
        implements PMap<K, V> {

    @Override
    public abstract This put(K key, V value);

    @Override
    public This putAll(PMap<? extends K, ? extends V> map) {
        This result = self();
        for (Entry<? extends K, ? extends V> entry : map.entrySet()) {
            result = result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    @Override
    public This putAll(Map<? extends K, ? extends V> map) {
        This result = self();
        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            result = result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    @Override
    public abstract This remove(Object key);

    protected This self() {
        @SuppressWarnings("unchecked")
        This self = (This) this;
        return self;
    }

    @Override
    public String toString() {
        Iterator<Entry<K, V>> iter = entrySet().iterator();
        if (!iter.hasNext()) {
            return "{}";
        }

        StringBuilder builder = new StringBuilder();
        builder.append('{');

        for (;;) {
            Entry<K, V> e = iter.next();
            K key = e.getKey();
            V value = e.getValue();

            builder.append(key == this ? "(this map)" : key);
            builder.append('=');
            builder.append(value == this ? "(this map)" : value);

            if (!iter.hasNext()) {
                return builder.append('}').toString();
            }
            builder.append(',').append(' ');
        }
    }

    @Override
    public int hashCode() {
        int hash = 0;

        Iterator<Entry<K,V>> iter = entrySet().iterator();
        while (iter.hasNext()) {
            hash += iter.next().hashCode();
        }

        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PMap<?, ?>)) {
            return false;
        }

        PMap<?, ?> that = (PMap<?, ?>) obj;
        if (this.size() != that.size()) {
            return false;
        }

        try {

            Iterator<Entry<K,V>> i = entrySet().iterator();
            while (i.hasNext()) {
                Entry<K,V> e = i.next();
                K key = e.getKey();
                V value = e.getValue();

                if (value == null) {
                    if (!that.containsKey(key)) {
                        return false;
                    }
                } else {
                    if (!value.equals(that.get(key))) {
                        return false;
                    }
                }
            }

        } catch (ClassCastException unused) {
            return false;
        } catch (NullPointerException unused) {
            return false;
        }

        return true;
    }
}
