package com.ebay.coding.assignment.dto;

import com.ebay.coding.assignment.util.PropertyUtil;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Least Recently used Cache for storing failed urls/files
 * This map is synchronized to allow access from multiple threads
 *
 * @param <K>
 * @param <V>
 */

public class DeadLetterQueue {

    private Map<String, DeadLetter> lruCache;
    private int maxSize;

    public DeadLetterQueue() {
        String maxSize = PropertyUtil.INSTANCE.getProperty("max.failure.backlog", "1000");
        int max = Integer.parseInt(maxSize);
        this.maxSize = max;
        lruCache = Collections.synchronizedMap(new LRUCache<>(max));
    }


    public boolean isFull() {
        return lruCache.size() >= maxSize;
    }

    public void put(String key, DeadLetter value) {
        lruCache.put(key, value);
    }

    public Set<Map.Entry<String, DeadLetter>> entrySet() {
        return lruCache.entrySet();
    }

    public DeadLetter get(String key) {
        return lruCache.get(key);
    }

    public DeadLetter getOrDefault(String key, DeadLetter def) {
        return lruCache.getOrDefault(key, def);
    }

    public void remove(String key) {
        lruCache.remove(key);
    }

    private static class LRUCache<K, V> extends LinkedHashMap<K, V> {
        final Integer max;

        public LRUCache(int maxSize) {
            this.max = maxSize;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return (this.size() > max); //must override it if used in a fixed cache
        }
    }

}

