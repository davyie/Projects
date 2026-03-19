package com.roadmap.cache.proxy;

import ch.qos.logback.core.net.server.Client;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.function.BiFunction;

@Configuration
public class Config {

    @Value("${global.origin}")
    private String origin;

    private Logger LOG = LoggerFactory.getLogger(Config.class);

    @Bean
    public BiFunction<HttpRequest, URI, HttpRequest> biFunction() {
        return (httpRequest, uri) -> new HttpRequest() {
            @Override
            public HttpMethod getMethod() {
                return httpRequest.getMethod();
            }

            @Override
            public URI getURI() {
                return uri;
            }

            @Override
            public Map<String, Object> getAttributes() {
                return Map.of();
            }

            @Override
            public HttpHeaders getHeaders() {
                return httpRequest.getHeaders();
            }
        };
    }

    @Bean
    public ClientHttpRequestInterceptor clientHttpRequestInterceptor(BiFunction<HttpRequest, URI, HttpRequest> createNewRequest) {
        return new ClientHttpRequestInterceptor() {
            @Override
            @NonNull
            public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

                request.getHeaders().add("Authorization", "Bearer ");

                ClientHttpResponse resp = execution.execute(request, body); // This passes the request to next interceptor or HTTP client

                LOG.info(resp.getStatusCode().toString());
                return resp;
            }
        };
    }

    @Bean
    public RestClient restClient(ClientHttpRequestInterceptor clientHttpRequestInterceptor) {
        return RestClient.builder().baseUrl(origin).requestInterceptor(clientHttpRequestInterceptor).build();
    }

    @Bean
    public RestClientAdapter restClientAdapter(RestClient restClient) {
        return RestClientAdapter.create(restClient);
    }

    @Bean
    public HttpServiceProxyFactory httpServiceProxyFactory(RestClientAdapter restClientAdapter) {
        return HttpServiceProxyFactory.builderFor(restClientAdapter).build();
    }

    @Bean
    public ProxyService proxyService(HttpServiceProxyFactory httpServiceProxyFactory) {
        return httpServiceProxyFactory.createClient(ProxyService.class);
    }
}
