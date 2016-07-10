package ru.home.rodionov.cache;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Implementation cache data structure based on {@link LinkedList}
 * store the data in RAM.
 */
public class RAMCache extends Cache {

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
        cache.addLast(element);
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
     * Clears the cache
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
     * Removes first element from cache
     *
     * @return removed element
     */
    @Override
    public CacheObject removeFirst() {
        CacheObject removedElement;
        try {
            lock.lock();
            removedElement = cache.removeFirst();
        } finally {
            lock.unlock();
        }
        return removedElement;
    }

    /**
     * Removes last element from cache
     *
     * @return removed element
     */
    @Override
    public CacheObject removeLast() {
        CacheObject removedElement;
        try {
            lock.lock();
            removedElement = cache.removeLast();
        } finally {
            lock.unlock();
        }
        return removedElement;
    }

    /**
     * @return elements count in this cache
     */
    @Override
    public int size() {
        removeNotActual();
        return cache.size();
    }

    /**
     * @param key
     * @return value by key or null if not exists
     */
    @Override
    public Object get(Object key) {
        removeNotActual();
        CacheObject element =
                cache.stream()
                        .filter(cacheObject -> key.equals(cacheObject.getKey()))
                        .findAny().orElse(null);
        if (element != null) {
            try {
                lock.lock();
                cache = algorithm.shift(cache, element);
            } finally {
                lock.unlock();
            }
        }
        return element == null ? null : element.getValue();
    }

    /**
     * method for removeLast all old elements
     *
     * @return actual {@link LinkedList}
     */
    @Override
    public LinkedList removeNotActual() {
        Iterator<CacheObject> it = cache.iterator();
        while (it.hasNext()) {
            if (it.next().getEndOfLife() < System.currentTimeMillis()) {
                try {
                    lock.lock();
                    it.remove();
                } finally {
                    lock.unlock();
                }
                }
            }
        return cache;
    }

    /**
     * @param key - key of the element
     * @return index o the value or -1 if this cache does not contain the element
     */
    @Override
    public int indexOf(Object key) {
        int i = 0;
        for (CacheObject obj : cache) {
            if (key.equals(obj.getKey())) return i;
            i++;
        }
        return -1;
    }

}

