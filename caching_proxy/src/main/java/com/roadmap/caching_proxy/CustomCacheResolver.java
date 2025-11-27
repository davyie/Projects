package com.roadmap.caching_proxy;

import org.springframework.cache.interceptor.CacheOperationInvocationContext;
import org.springframework.cache.interceptor.CacheResolver;

import java.util.Collection;

public class CustomCacheResolver implements CacheResolver {


    private static final int DEFAULT_TTL = 30;

    private CacheResolver delegate;

    public CustomCacheResolver(CacheResolver cacheResolver) {
        this.delegate = cacheResolver;
    }

    public Collection<CustomHeaderResponseCache> resolveCaches(CacheOperationInvocationContext context) {
        return this.delegate.resolveCaches(context).stream().map(CustomHeaderResponseCache::new).toList();
    }
}
