package com.roadmap.cache.proxy;

import org.springframework.http.ResponseEntity;
import org.springframework.web.service.annotation.GetExchange;

public interface ProxyService {
    @GetExchange("/products")
    ResponseEntity<String> getProducts();
}
