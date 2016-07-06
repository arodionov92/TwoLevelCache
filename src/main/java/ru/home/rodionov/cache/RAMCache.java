package ru.home.rodionov.cache;

import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Implementation cache data structure based on {@link LinkedList}
 * store the data in RAM.
 */
public class RAMCache implements ICache {

    private LinkedList<CacheObject> cache;
    private CacheAlgorithm algorithm;
    private ReentrantLock lock = new ReentrantLock();
    private long TTL;

    /**
     * @param algorithm - any implementation of {@link CacheAlgorithm}
     * @param TTL       - storage time for elements in milliseconds
     */
    public RAMCache(CacheAlgorithm algorithm, long TTL) {
        this.algorithm = algorithm;
        this.cache = new LinkedList();
        this.TTL = TTL;

    }

    /**
     * Create new element with key-value and appends it to the end of this cache
     *
     * @param key   - key
     * @param value - value
     */
    @Override
    public void add(Object key, Object value) {
        CacheObject element = new CacheObject(key, value, TTL);
        try {
            lock.lock();
            cache.add(element);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Inserts element in the beginning of this cache
     *
     * @param element - element
     */
    @Override
    public void addFirst(CacheObject element) {
        removeNotActual();
        try {
            lock.lock();
            cache.addFirst(element);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Append element to the end of this cache
     *
     * @param element
     */
    @Override
    public void addLast(CacheObject element) {
        removeNotActual();
        try {
            lock.lock();
            cache.add(element);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Clears cache
     */
    @Override
    public void clear() {
        try {
            lock.lock();
            cache.clear();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Removes element by index
     *
     * @param index
     * @return removed element
     */
    @Override
    public CacheObject remove(int index) {
        CacheObject removedObject;
        try {
            lock.lock();
            removedObject = cache.remove(index);
        } finally {
            lock.unlock();
        }
        return removedObject;
    }

    /**
     * @return elements count in this cache
     */
    @Override
    public int size() {
        return cache.size();
    }

    /**
     * @param key
     * @return value by key
     */
    @Override
    public Object get(Object key) {
        int elemIndex = 0;
        CacheObject element = null;

        for (int i = 0; i < cache.size(); i++) {
            CacheObject obj = cache.get(i);
            if (obj.getKey().equals(key)) {
                elemIndex = i;
                element = obj;
                break;
            }
        }

        if (elemIndex > 0) {
            try {
                lock.lock();
                cache = algorithm.shift(cache, elemIndex);
            } finally {
                lock.unlock();
            }
        }
        return element == null ? null : element.getValue();
    }

    /**
     * method for remove all old elements
     *
     * @return actual {@link LinkedList}
     */
    @Override
    public LinkedList removeNotActual() {
        for (int i = 0; i < cache.size(); i++) {
            if (cache.get(i).getEndOfLife() < System.currentTimeMillis()) {
                try {
                    lock.lock();
                    cache.remove(i);
                } finally {
                    lock.unlock();
                }
            }
        }
        return cache;
    }

    /**
     * @param key - key of the element
     * @return index of the value or -1 if this cache does not contain the element
     */
    @Override
    public int indexOf(Object key) {
        for (int i = 0; i < cache.size(); i++) {
            if (key.equals(cache.get(i).getKey())) {
                return i;
            }
        }
        return -1;
    }

}

