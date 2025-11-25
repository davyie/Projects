package com.roadmap.caching_proxy;

import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static com.roadmap.caching_proxy.CacheHeader.HIT;
import static java.util.stream.Collectors.toUnmodifiableMap;

public class CachedResponseEntity extends ResponseEntity<String> {

    public String X_CACHE = "X-Cache";
    private ResponseEntity<String> delegate;

    public CachedResponseEntity(ResponseEntity<String> value) {
        super(value.getBody(), value.getStatusCode());
        this.delegate = value;
    }

    @Override
    @NonNull
    public HttpHeaders getHeaders() {
        Map<String, List<String>> originalHeaders = this.delegate.getHeaders()
                .headerSet()
                .stream()
                .filter( e -> !e.getKey().equals(X_CACHE))
                .collect(toUnmodifiableMap(Map.Entry::getKey,
                        Map.Entry::getValue));

        HttpHeaders cachedInfoHeaders = new HttpHeaders();
        cachedInfoHeaders.putAll(originalHeaders);
        cachedInfoHeaders.add(X_CACHE, HIT.toString());
        return cachedInfoHeaders;
    }
}
