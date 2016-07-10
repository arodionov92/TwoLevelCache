package ru.home.rodionov.cache;

import java.io.Serializable;
import java.util.LinkedList;

/**
 * Cache data structure
 */
public abstract class Cache<K extends Object & Serializable, V extends Object & Serializable> {

    abstract void add(K key, V value);

    abstract void addFirst(CacheObject obj);

    abstract void addLast(CacheObject element);

    abstract void clear();

    abstract CacheObject removeFirst();

    abstract CacheObject removeLast();

    abstract int size();

    abstract V get(K key);

    abstract LinkedList removeNotActual();

    abstract int indexOf(K key);
}
