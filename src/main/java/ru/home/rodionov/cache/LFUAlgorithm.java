package ru.home.rodionov.cache;

import java.util.LinkedList;

public class LFUAlgorithm implements CacheAlgorithm {
    @Override
    public LinkedList shift(LinkedList source, int key) {
        if (key > 0) {
            Object shiftedElement = source.get(key);
            source.set(key, source.get(key - 1));
            source.set(key - 1, shiftedElement);
        }

        return source;
    }
}
