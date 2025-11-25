package com.roadmap.caching_proxy;

import org.springframework.http.ResponseEntity;
import org.springframework.web.service.annotation.GetExchange;


public interface ProxyService {

    @GetExchange(value = "/products")
    ResponseEntity<String> getProducts();
}
