package ru.home.rodionov.cache;

import java.util.LinkedList;

/**
 * Displacement algorithm for cache
 */
public abstract class CacheAlgorithm {
    /**
     * @param source - {@link LinkedList} of {@link CacheObject} for changing
     * @param key    - index of element for shifting
     * @return - changed  {@link LinkedList} of {@link CacheObject}
     */
    abstract LinkedList<CacheObject> shift(LinkedList<CacheObject> source, CacheObject key);
}
