package com.roadmap.caching_proxy;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

    private ProxyService proxyService;

    public Controller(ProxyService proxyService) {
        this.proxyService = proxyService;
    }

    @GetMapping("/products")
    @Cacheable(cacheNames = "customCache", cacheResolver = "cacheResolver", key = "#root.methodName")
    public ResponseEntity<String> products() {
        return proxyService.getProducts();
    }
}
