package ru.home.rodionov.cache;

import java.util.LinkedList;

public class TwoLevelCache implements ICache {
    private ICache firstLevel;
    private ICache secondLevel;
    private int firstLevelMaxSize;
    private int secondLevelMaxSize;

    /**
     * @param firstLevel      - any implementation of {@link ICache}
     * @param secondLevel     - any implementation of {@link ICache}
     * @param firstLevelSize  - max size of first level
     * @param secondLevelSize - max size of second level
     */
    public TwoLevelCache(ICache firstLevel, ICache secondLevel, int firstLevelSize, int secondLevelSize) {
        this.firstLevel = firstLevel;
        this.secondLevel = secondLevel;
        this.firstLevelMaxSize = firstLevelSize;
        this.secondLevelMaxSize = secondLevelSize;
    }

    /**
     * Create new element with key-value and appends it to the end of this cache
     *
     * @param key   - key
     * @param value - value
     */
    @Override
    public void add(Object key, Object value) {
        if (firstLevel.size() < firstLevelMaxSize) {
            firstLevel.add(key, value);
        } else {
            if (secondLevel.size() >= secondLevelMaxSize) {
                secondLevel.remove(secondLevelMaxSize - 1);
            }
            CacheObject shiftedElement = firstLevel.remove(firstLevelMaxSize - 1);
            secondLevel.addFirst(shiftedElement);
            firstLevel.add(key, value);
        }
    }

    /**
     * Inserts element in the beginning of this cache
     *
     * @param element - element
     */
    @Override
    public void addFirst(CacheObject element) {
        firstLevel.addFirst(element);
    }

    /**
     * Append element to the end of this cache
     *
     * @param element
     */
    @Override
    public void addLast(CacheObject element) {
        secondLevel.addLast(element);
    }

    /**
     * Clears cache
     */
    @Override
    public void clear() {
        firstLevel.clear();
        secondLevel.clear();
    }

    /**
     * Removes element by index
     *
     * @param index
     * @return removed element
     */
    @Override
    public CacheObject remove(int index) {
        removeNotActual();
        if (index <= firstLevelMaxSize - 1) {
            return firstLevel.remove(index);
        } else if ((index - firstLevelMaxSize) < secondLevelMaxSize - 1) {
            return secondLevel.remove(index);
        } else throw new IndexOutOfBoundsException();
    }

    /**
     * @return elements count in this cache
     */
    @Override
    public int size() {
        return firstLevel.size() + secondLevel.size();
    }

    /**
     * @param key
     * @return value by key
     */
    @Override
    public Object get(Object key) {
        Object tempValue = firstLevel.get(key);

        if (tempValue == null) {
            tempValue = secondLevel.get(key);
            if (secondLevel.indexOf(key) == 0) {
                CacheObject shiftedElement = secondLevel.remove(0);
                secondLevel.addFirst(firstLevel.remove(firstLevelMaxSize - 1));
                firstLevel.addLast(shiftedElement);
                firstLevel.get(shiftedElement.getKey());
            }
        }
        return tempValue;
    }

    /**
     * method for remove all old elements
     *
     * @return actual {@link LinkedList}
     */
    @Override
    public LinkedList removeNotActual() {
        firstLevel.removeNotActual();
        secondLevel.removeNotActual();
        if (secondLevel.size() > 0) {
            shift();
        }
        return null;
    }

    /**
     * @param key - key of the element
     * @return index of the value or -1 if this cache does not contain the element
     */
    @Override
    public int indexOf(Object key) {
        if (firstLevel.indexOf(key) >= 0) {
            return firstLevel.indexOf(key);
        } else if (secondLevel.indexOf(key) >= 0) {
            return secondLevel.indexOf(key) + firstLevelMaxSize;
        }
        return -1;
    }

    /**
     * Shifts elements from second level to first if possible
     */
    private void shift() {
        int shiftRange = firstLevelMaxSize - firstLevel.size();
        for (int i = 1; i < shiftRange; i++) {
            if (secondLevel.size() > 0) {
                firstLevel.addLast(secondLevel.remove(0));
            }
        }
    }
}
