package ru.home.rodionov.cache;

import java.io.*;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Implementation cache data structure based on file
 */
public class FileCache implements ICache {

    private CacheAlgorithm algorithm;
    private File file;
    private AtomicInteger size;
    private ReentrantLock lock;
    private long TTL;

    /**
     * @param filepath  - path to create a cache file, include file extension (example: D:\cache.ch)
     * @param algorithm - any implementation of {@link CacheAlgorithm}
     * @param TTL       - storage time for elements in milliseconds
     */
    public FileCache(String filepath, CacheAlgorithm algorithm, long TTL) {
        this.file = new File(filepath);
        this.algorithm = algorithm;
        this.TTL = TTL;
        this.size = new AtomicInteger(0);
        this.lock = new ReentrantLock();

        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create new element with key-value and appends it to the end of this cache
     *
     * @param key   - key
     * @param value - value
     */
    @Override
    public void add(Object key, Object value) {
        CacheObject element = new CacheObject(key, value, TTL);
        addLast(element);
        size.getAndIncrement();
    }

    /**
     * Inserts element in the beginning of this cache
     *
     * @param element - element
     */
    @Override
    public void addFirst(CacheObject element) {
        removeNotActual();
        LinkedList list = getListFromFile();
        try {
            lock.lock();
            list.addFirst(element);
        } finally {
            lock.unlock();
        }
        rewriteFileWithList(list);
        size.getAndIncrement();
    }

    /**
     * Append element to the end of this cache
     *
     * @param element
     */
    @Override
    public void addLast(CacheObject element) {
        removeNotActual();
        LinkedList<CacheObject> list = getListFromFile();
        try {
            lock.lock();
            list.addLast(element);
        } finally {
            lock.unlock();
        }
        rewriteFileWithList(list);
        size.getAndIncrement();
    }

    /**
     * Clears file
     */
    @Override
    public void clear() {
        try {
            lock.lock();
            ObjectOutput output = getOutput();
            output.write((new String()).getBytes());
            output.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        size.getAndIncrement();
    }

    /**
     * Removes element by index
     *
     * @param index
     * @return removed element
     */
    @Override
    public CacheObject remove(int index) {
        LinkedList<CacheObject> buffer = getListFromFile();
        CacheObject removedObject;
        try {
            lock.lock();
            removedObject = buffer.remove(index);
        } finally {
            lock.unlock();
        }
        rewriteFileWithList(buffer);
        size.decrementAndGet();
        return removedObject;
    }

    /**
     * @return elements count in this cache
     */
    @Override
    public int size() {
        return size.get();
    }

    /**
     * @param key
     * @return value by key
     */
    @Override
    public Object get(Object key) {
        LinkedList<CacheObject> buffer = getListFromFile();
        CacheObject element = null;
        for (int i = 0; i < size.get(); i++) {
            if (key.equals(buffer.get(i).getKey())) {
                element = buffer.get(i);
                try {
                    lock.lock();
                    buffer = algorithm.shift(buffer, i);
                } finally {
                    lock.unlock();
                }
                rewriteFileWithList(buffer);
            }
        }
        return element == null ? null : element.getValue();
    }

    /**
     * method for remove all old elements
     *
     * @return actual {@link LinkedList}
     */
    public LinkedList removeNotActual() {
        LinkedList list = getListFromFile();
        for (int i = 0; i < list.size(); i++) {
            CacheObject element = (CacheObject) list.get(i);
            if (element.getEndOfLife() < System.currentTimeMillis()) {
                try {
                    lock.lock();
                    list.remove(i);
                } finally {
                    lock.unlock();
                }
            }
        }
        rewriteFileWithList(list);
        size.set(list.size());
        return list;
    }

    /**
     * @param key - key of the element
     * @return index of the value or -1 if this cache does not contain the element
     */
    @Override
    public int indexOf(Object key) {
        LinkedList<CacheObject> list = getListFromFile();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getKey().equals(key)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Read the cache file
     *
     * @return cache objects as {@link LinkedList}
     */
    private LinkedList getListFromFile() {
        LinkedList<CacheObject> list = new LinkedList();
        if (size.get() > 0) {
            try {
                lock.lock();
                try (ObjectInputStream input = getInput()) {
                    list = (LinkedList<CacheObject>) input.readObject();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            } finally {
                lock.unlock();
            }
        }
        return list;
    }

    /**
     * @return input stream from file
     */
    private ObjectInputStream getInput() {
        ObjectInputStream input = null;
        try {
            FileInputStream fin = new FileInputStream(file);
            BufferedInputStream buffer = new BufferedInputStream(fin);
            input = new ObjectInputStream(buffer);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return input;
    }

    /**
     * @return output stream to file
     */
    private ObjectOutputStream getOutput() {
        ObjectOutputStream output = null;
        try {
            FileOutputStream fou = new FileOutputStream(file);
            BufferedOutputStream buffer = new BufferedOutputStream(fou);
            output = new ObjectOutputStream(buffer);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output;
    }

    /**
     * Override the cache file
     *
     * @param buffer updated {@link LinkedList}
     */
    private void rewriteFileWithList(LinkedList buffer) {
        try {
            lock.lock();
            try (ObjectOutputStream output = getOutput()) {
                output.writeObject(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } finally {
            lock.unlock();
        }
    }
}
