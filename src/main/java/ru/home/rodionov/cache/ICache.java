package ru.home.rodionov.cache;

import java.util.LinkedList;

/**
 * Cache data structure
 */
public interface ICache {
    void add(Object key, Object value);

    void addFirst(CacheObject obj);

    void addLast(CacheObject element);

    void clear();

    CacheObject remove(int index);

    int size();

    Object get(Object key);

    LinkedList removeNotActual();

    int indexOf(Object key);
}
