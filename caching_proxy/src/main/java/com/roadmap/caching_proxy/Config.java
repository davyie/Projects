package com.roadmap.caching_proxy;

import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.SimpleCacheResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

import static com.roadmap.caching_proxy.CacheHeader.MISS;

@Configuration
@EnableCaching
public class Config implements CachingConfigurer {

    private Logger LOG = LoggerFactory.getLogger(Config.class);

    @Value("${global.origin}") // Fetch this value from application.properties
    private String origin;

    private final int TTL = 5;

    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager() {
            @Override
            protected Cache createConcurrentMapCache(final String name) {
                return new ConcurrentMapCache(
                        name,
                        CacheBuilder.newBuilder()
                                .expireAfterWrite(TTL, TimeUnit.SECONDS)
                                .maximumSize(100)
                                .build()
                                .asMap(),
                        false
                );
            }
        };
        return cacheManager;
    }

    @Bean
    public CacheResolver cacheResolver(CacheManager cacheManager) {
        LOG.info("CustomCacheResolver initialized");
        return new CustomCacheResolver(new SimpleCacheResolver(cacheManager));
    }

    private ClientHttpResponse getResponseWithXCacheMissHeader(final ClientHttpResponse resp) {
        return new ClientHttpResponse() {
            @Override
            public HttpHeaders getHeaders() {
                final HttpHeaders originalHeaders = resp.getHeaders();
                final HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.put("X-Cache", List.of(MISS.toString()));
                httpHeaders.putAll(originalHeaders);
                return httpHeaders;
            }

            @Override
            public InputStream getBody() throws IOException {
                return resp.getBody();
            }

            @Override
            public HttpStatusCode getStatusCode() throws IOException {
                return resp.getStatusCode();
            }

            @Override
            public String getStatusText() throws IOException {
                return resp.getStatusText();
            }

            @Override
            public void close() {
                resp.close();
            }
        };
    }

    @Bean
    public BiFunction<HttpRequest, URI, HttpRequest> httpRequestBiFunction() {
        return (request, uri) -> new HttpRequest() {
            @Override
            public HttpMethod getMethod() {
                return request.getMethod();
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
                return request.getHeaders();
            }
        };
    }

    @Bean
    public ClientHttpRequestInterceptor clientHttpRequestInterceptor(final BiFunction<HttpRequest, URI, HttpRequest> createNewRequest) {
        return new ClientHttpRequestInterceptor() {
            @Override
            @NonNull
            public ClientHttpResponse intercept(@NonNull final HttpRequest request,
                                                @NonNull final byte[] body,
                                                @NonNull final ClientHttpRequestExecution execution) throws IOException {
                ClientHttpResponse resp = execution.execute(request, body);
                LOG.info("This is the status code from execute() {} ", resp.getStatusCode().toString());
                while (resp.getStatusCode().is3xxRedirection()) {
                    final var location = resp.getHeaders().getLocation().toString();
                    LOG.info(location);
                    final URI uri = UriComponentsBuilder.fromUriString(location).build().toUri();
                    final var newRequest = createNewRequest.apply(request, uri); //
                    LOG.info("new URI: {}", uri.toString());
                    LOG.info("This is the status code from createNewRequest.apply() {} ", resp.getStatusCode().toString());
                    resp = execution.execute(newRequest, body);
                }
                LOG.info("This is the status code after everything {} ", resp.getStatusCode().toString());
                return getResponseWithXCacheMissHeader(resp);
            }
        };
    }

    /**
     * Create a RestClient bean into Application Context which can be used to send requests to
     * origin url.
     * @param interceptor
     * @return
     */
    @Bean
    public RestClient restClient(ClientHttpRequestInterceptor interceptor) {
        return RestClient.builder().baseUrl(origin).requestInterceptor(interceptor).build();
    }

    /**
     * We create a adapter between our proxy object and the rest client bean which allow us to send request
     * using our proxy instead of the rest client bean. This allows us to inject custom behaviours in the response
     * header.
     * * @param restClient
     * @return
     */
    @Bean
    public RestClientAdapter restClientAdapter(RestClient restClient) {
        return RestClientAdapter.create(restClient);
    }

    /**
     * This is a method to create a proxy bean for RestClient.
     * @param adapter
     * @return
     */
    @Bean
    public HttpServiceProxyFactory httpServiceProxyFactory(RestClientAdapter adapter) {
        return HttpServiceProxyFactory.builderFor(adapter).build();
    }

    /**
     * This is literally the bean creation for our ProxyService. It makes use of the Factory to produce a valid bean which
     * we can use in our code.
     * @param httpServiceProxyFactory
     * @return
     */
    @Bean
    public ProxyService proxyService(HttpServiceProxyFactory httpServiceProxyFactory) {
        return httpServiceProxyFactory.createClient(ProxyService.class);
    }
}
