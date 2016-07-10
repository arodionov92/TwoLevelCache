package ru.home.rodionov.cache;

import java.io.Serializable;

/**
 * Data structure for store any data inside cache as key-value
 *
 * @param <K> - parametric key
 * @param <V> - parametric value
 */
public class CacheObject<K extends Object & Serializable, V extends Object & Serializable> implements Serializable {
    private static final long serialVersionUID = 20160704L;
    private K key;
    private V value;
    private long endOfLife;

    /**
     * @param key   - key
     * @param value - value
     * @param TTL   - storage time in milliseconds
     */
    CacheObject(K key, V value, long TTL) {
        this.key = key;
        this.value = value;
        endOfLife = System.currentTimeMillis() + TTL;
    }

    long getEndOfLife() {
        return endOfLife;
    }

    K getKey() {
        return key;
    }

    V getValue() {
        return value;
    }
}

