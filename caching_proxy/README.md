# Getting Started

## Learning Objects 
- Proxy 
- Caching 
- Response Headers 

This project has been created such that we learn how Proxy, Caching and ResponseEntity works in Spring Boot.

### Part 1 - Proxy 
What is proxy? 
This is the concept of creating psuedo objects in front of the real objects. This can be used to implement custom behaviours and properties. 
How is this implemented? 
We usually want to it by implementing existing Spring classes and implement the interface. 

Let's start of with creating Proxy class for RestClient. The Proxy is then added to application context by using a Factory and Adapter to connect it to the "real" object which is RestClient. 
We also have to add Interceptor which enables us to add a custom header which states that the request is either HIT or MISS in cache. The interceptor works like this, it intercepts the request before rest client sends it out. Then it will send the request towards rest client. The rest client sends request and receives response. Then interceptor fetches the response and add stuff to it then it will return to our code. 
My code --> Interceptor --> RestClient --> Interceptor --> My code. 

### Part 2 - Caching 
The idea is to add a header a HIT or MISS property. We need to add a Custom Resolver, Custom HeaderResponse Cache, ResponseEntityCache and CacheHeader, CachedResponseEntity. We add @Cachable annotation which caches the data from a method. The annotation takes three arguments, cacheName, cacheResolver and key. The idea behind these is cacheName tells us which cache the data is stored in. CacheResolver is the object that handles then checking in cache and key is the key-value pair that is used to fetch the value from cache. 

Now that we have added the annotation we have to configure our resolver. This resolver will extend CacheResolver and act as a proxy for it. This resolver will decorate each cache with custom properties. This is so the caches will have custom functionality. That is we want to store the response in the cache. We have a custom ResponseEntityCache which we implement in a custom response cache. This means we have to implement the .put() function to store a custom ResponseEntity which adds a new header to the response. 

### Why do we implement it like this? 
Proxy is a major part of Spring and it is what powers the functionality of Spring. We want to learn more about how we can utilize proxy and customize them to fit our problem set. Proxy is a powerful tool which helps us add functionality to existing components of Spring, in this case it is RestClient and CacheResolver. Instead of working with the underlying tool we build on top of it with customized functionality to fit our need. 
Caching can be implemented with a HashMap but it becomes troublesome when the cache grows. This is why we use Spring's implementation of Caching and extend it such that we discard complexity. 

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/4.0.0/maven-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/4.0.0/maven-plugin/build-image.html)

### Maven Parent overrides

Due to Maven's design, elements are inherited from the parent POM to the project POM.
While most of the inheritance is fine, it also inherits unwanted elements like `<license>` and `<developers>` from the parent.
To prevent this, the project POM contains empty overrides for these elements.
If you manually switch to a different parent and actually want the inheritance, you need to remove those overrides.

