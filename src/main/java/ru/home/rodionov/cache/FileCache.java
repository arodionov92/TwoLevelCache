package ru.home.rodionov.cache;

import java.io.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Implementation cache data structure based on file
 */
public class FileCache extends Cache {

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
            System.err.println(e.getMessage());
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
    @SuppressWarnings("unchecked")
    //TODO rework to type safe
    public void add(Object key, Object value) {
        CacheObject element = new CacheObject(key, value, TTL);
        addLast(element);
    }

    /**
     * Inserts element in the beginning of this cache
     *
     * @param element - element
     */
    @Override
    public void addFirst(CacheObject element) {
        removeNotActual();
        LinkedList<CacheObject> list = getListFromFile();
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
     * @param element - the element to add
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
            output.write("".getBytes());
            output.close();
        } catch (FileNotFoundException e) {
            System.err.println("File not found");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Error while clearing cache");
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        size.set(0);
    }

    /**
     * Removes first element from cache
     *
     * @return removed element
     */
    @Override
    public CacheObject removeFirst() {
        LinkedList<CacheObject> buffer = removeNotActual();
        CacheObject removedElement;
        try {
            lock.lock();
            removedElement = buffer.removeFirst();
        } finally {
            lock.unlock();
        }
        rewriteFileWithList(buffer);
        size.decrementAndGet();
        return removedElement;
    }

    /**
     * Removes last element from cache
     *
     * @return removed element
     */
    @Override
    public CacheObject removeLast() {
        LinkedList<CacheObject> buffer = removeNotActual();
        CacheObject removedElement;
        try {
            lock.lock();
            removedElement = buffer.removeLast();
        } finally {
            lock.unlock();
        }
        rewriteFileWithList(buffer);
        size.decrementAndGet();
        return removedElement;
    }

    /**
     * @return elements count in this cache
     */
    @Override
    public int size() {
        removeNotActual();
        return size.get();
    }

    /**
     * @param key - key
     * @return value by key
     */
    @Override
    public Object get(Object key) {
        LinkedList<CacheObject> buffer = removeNotActual();
        CacheObject element =
                buffer.parallelStream()
                        .filter(cacheObject -> key.equals(cacheObject.getKey()))
                        .findFirst().orElse(null);

        if (element != null) {
            try {
                lock.lock();
                buffer = algorithm.shift(buffer, element);
            } finally {
                lock.unlock();
            }
            rewriteFileWithList(buffer);
        }

        return element == null ? null : element.getValue();
    }

    /**
     * method for removeLast all old elements
     *
     * @return actual {@link LinkedList}
     */
    public LinkedList<CacheObject> removeNotActual() {
        LinkedList<CacheObject> list = getListFromFile();
        Iterator<CacheObject> it = list.iterator();
        while (it.hasNext()) {
            if (it.next().getEndOfLife() < System.currentTimeMillis()) {
                try {
                    lock.lock();
                    it.remove();
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
        LinkedList<CacheObject> list = removeNotActual();
        int i = 0;
        for (CacheObject obj : list) {
            if (key.equals(obj.getKey())) return i;
            i++;
        }
        return -1;
    }

    /**
     * Read the cache file
     *
     * @return cache objects as {@link LinkedList}
     */
    @SuppressWarnings("unchecked")
    private LinkedList<CacheObject> getListFromFile() {
        LinkedList<CacheObject> list = new LinkedList<>();
        if (size.get() > 0) {
            try {
                lock.lock();
                try (ObjectInputStream input = getInput()) {
                    list = (LinkedList<CacheObject>) input.readObject();
                } catch (ClassNotFoundException | IOException e) {
                    e.printStackTrace();
                }
            } finally {
                lock.unlock();
            }
        }
        return list;
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

    /**
     * @return input stream from file
     */
    private ObjectInputStream getInput() {
        ObjectInputStream input = null;
        try {
            FileInputStream fin = new FileInputStream(file);
            BufferedInputStream buffer = new BufferedInputStream(fin);
            input = new ObjectInputStream(buffer);
        } catch (IOException e) {
            System.err.println("");
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output;
    }
}
