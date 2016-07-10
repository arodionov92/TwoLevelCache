package ru.home.rodionov.cache;

import java.util.LinkedList;

/**
 * Cache data structure
 */
public abstract class Cache {

    abstract void add(Object key, Object value);

    abstract void addFirst(CacheObject obj);

    abstract void addLast(CacheObject element);

    abstract void clear();

    abstract CacheObject removeFirst();

    abstract CacheObject removeLast();

    abstract int size();

    abstract Object get(Object key);

    abstract LinkedList removeNotActual();

    abstract int indexOf(Object key);
}
