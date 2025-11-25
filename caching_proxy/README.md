# Getting Started

## Learning Objects 
- Proxy 
- Caching 
- Response Headers 

This project has been created such that we learn how Proxy, Caching and ResponseEntity works in Spring Boot.

### Proxy 
What is proxy? 
This is the concept of creating psuedo objects in front of the real objects. This can be used to implement custom behaviours and properties. 
How is this implemented? 
We usually want to it by implementing existing Spring classes and implement the interface. 

Let's start of with creating Proxy class for RestClient. 

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

