package ru.home.rodionov.cache;

import java.util.LinkedList;

/**
 * Implementation of "least frequently used" algorithm
 */
public class LRUAlgorithm extends CacheAlgorithm {

    /**
     * @param source  - {@link LinkedList} for changing
     * @param element - element for shifting
     * @return shifted {@link LinkedList}
     */
    @Override
    public LinkedList<CacheObject> shift(LinkedList<CacheObject> source, CacheObject element) {
        source.remove(element);
        source.addFirst(element);
        return source;
    }
}

