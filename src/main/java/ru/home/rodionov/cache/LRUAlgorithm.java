package ru.home.rodionov.cache;

import java.util.LinkedList;

/**
 * Implementation of "least recently used" algorithm
 */
public class LRUAlgorithm implements CacheAlgorithm {
    /**
     *
     * @param source - {@link LinkedList} for changing
     * @param key - index of element for shifting
     * @return
     */
    @Override
    public LinkedList shift(LinkedList source, int key) {
        source.addFirst(source.remove(key));
        return source;
    }
}

