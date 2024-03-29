package com.platypii.avyalert.data;

import java.util.LinkedHashMap;


/**
 * Implements a Least Recently Used Cache using a Java LinkedHashMap
 * @author platypii
 */
public class LRUCache<K,V> extends LinkedHashMap<K,V> {
    private static final long serialVersionUID = -676863887105210242L;

    private int maxSize = 100;
    public LRUCache(int maxSize) {
        super(maxSize, 0.75f, true);
        this.maxSize = maxSize;
    }
    @Override
    protected boolean removeEldestEntry(Entry<K,V> entry) {
        return size() > maxSize;
    }
}