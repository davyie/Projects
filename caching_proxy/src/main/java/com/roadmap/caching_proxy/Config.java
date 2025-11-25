package com.roadmap.caching_proxy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class Config {
    @Value("${global.origin}") // Fetch this value as a flag when running the application
    private String origin;

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

    @Bean
    public HttpServiceProxyFactory httpServiceProxyFactory(RestClientAdapter adapter) {
        return HttpServiceProxyFactory.builderFor(adapter).build();
    }

    @Bean
    public ProxyService proxyService(HttpServiceProxyFactory httpServiceProxyFactory) {
        return httpServiceProxyFactory.createClient(ProxyService.class);
    }
}
