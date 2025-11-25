package com.roadmap.caching_proxy;


import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.cache.Cache;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.Callable;

public class CustomHeaderResponseCache implements ResponseEntityCache {

    private Cache delegate;

    public CustomHeaderResponseCache(Cache cache) {
        this.delegate = cache;
    }

    @Override
    public void put(@NonNull Object key, ResponseEntity<String> value) {
        this.delegate.put(key, new CachedResponseEntity(value));
    }

    @Override
    @NonNull
    public String getName() {
        return this.delegate.getName();
    }

    @Override
    public Object getNativeCache() {
        return this.delegate.getNativeCache();
    }

    @Override
    public @Nullable ValueWrapper get(@NonNull Object key) {
        return this.delegate.get(key);
    }

    @Override
    public <T> @Nullable T get(@NonNull Object key, Class<T> type) {
        return this.delegate.get(key, type);
    }

    @Override
    public <T> @Nullable T get(@NonNull Object key, Callable<T> valueLoader) {
        return this.delegate.get(key, valueLoader);
    }

    @Override
    public void evict(@NonNull Object key) {
        this.delegate.evict(key);
    }

    @Override
    public void clear() {
        this.delegate.clear();
    }
}
