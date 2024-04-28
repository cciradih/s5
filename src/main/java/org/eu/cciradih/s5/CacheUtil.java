package org.eu.cciradih.s5;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

public record CacheUtil(Cache<String, Object> cache) {
    private static class CacheUtilHolder {
        private static final Cache<String, Object> CACHE = Caffeine.newBuilder().build();
        private static final CacheUtil INSTANCE = new CacheUtil(CACHE);
    }

    public static CacheUtil getInstance() {
        return CacheUtilHolder.INSTANCE;
    }

    public void put(String key, Object value) {
        this.cache.put(key, value);
    }

    public Object get(String key) {
        return this.cache.getIfPresent(key);
    }
}
