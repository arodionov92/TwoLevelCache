package ru.home.rodionov.cache;

import java.util.LinkedList;

/**
 * Displacement algorithm for cache
 *
 * @param <K> any implementation of {@link LinkedList}
 */
public abstract class CacheAlgorithm<K extends LinkedList> {
    /**
     * @param source - {@link LinkedList} for changing
     * @param key    - index of element for shifting
     * @return - changed {@link LinkedList}
     */
    abstract K shift(LinkedList source, CacheObject key);
}
