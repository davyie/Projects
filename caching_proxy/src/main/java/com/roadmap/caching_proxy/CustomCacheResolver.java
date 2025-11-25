package com.roadmap.caching_proxy;


import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.http.ResponseEntity;

import java.util.Collection;

public class CustomCacheResolver implements CacheResolver {

    private CacheResolver delegate;

    public CustomCacheResolver(CacheResolver cacheResolver) {
        this.delegate = cacheResolver;
    }

    public Collection<CustomHeaderResponseCache> resolveCaches(CacheOperationInvocationContext context) {
        return this.delegate.resolveCaches(context).stream().map(CustomHeaderResponseCache::new).toList();
    }
}
