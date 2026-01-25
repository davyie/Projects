package com.roadmap.cache.proxy;

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
    public ResponseEntity<String> getProducts() {
        return proxyService.getProducts();
    }

}
