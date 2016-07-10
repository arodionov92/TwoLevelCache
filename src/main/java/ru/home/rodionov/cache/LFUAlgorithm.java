package ru.home.rodionov.cache;

import java.util.LinkedList;

/**
 * Implementation of "least recently used" algorithm
 */
public class LFUAlgorithm extends CacheAlgorithm {

    /**
     * @param source  - {@link LinkedList} for changing
     * @param element - element for shifting
     * @return
     */
    @Override
    public LinkedList shift(LinkedList source, CacheObject element) {
        int index = source.indexOf(element);
        if (index != 0) {
            source.set(index, source.get(index - 1));
            source.set(index - 1, element);
        }
        return source;
    }
}
