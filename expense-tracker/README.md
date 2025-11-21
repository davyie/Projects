# Read Me First
The following was discovered as part of building this project:

* The original package name 'com.davyie.expense-tracker' is invalid and this project uses 'com.davyie.expense_tracker' instead.

# Getting Started

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/3.5.7/maven-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/3.5.7/maven-plugin/build-image.html)
* [Spring Web](https://docs.spring.io/spring-boot/3.5.7/reference/web/servlet.html)
* [Spring Security](https://docs.spring.io/spring-boot/3.5.7/reference/web/spring-security.html)

### Guides
The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)
* [Securing a Web Application](https://spring.io/guides/gs/securing-web/)
* [Spring Boot and OAuth2](https://spring.io/guides/tutorials/spring-boot-oauth2/)
* [Authenticating a User with LDAP](https://spring.io/guides/gs/authenticating-ldap/)

### Maven Parent overrides

Due to Maven's design, elements are inherited from the parent POM to the project POM.
While most of the inheritance is fine, it also inherits unwanted elements like `<license>` and `<developers>` from the parent.
To prevent this, the project POM contains empty overrides for these elements.
If you manually switch to a different parent and actually want the inheritance, you need to remove those overrides.

### Software 
* Maven 
* Java 21 
* Docker Engine
* Docker Desktop
* Unix environment 

### How to Build 
To build the project, ensure that Docker Desktop is up and running or equivalent Docker engine. Run the command `mvn clean install`. The command produces a docker image named `expense-tracker:latest` which is used in `docker-compose.yml. 
run `docker compose up -d` to create a container from the newly created docker image. 

### Capabilities of Expense Tracker 
The functionality of the software is to 
- [x] add expense 
- [x] update expense
- [x] delete expense 
- [] save expenses 

### Learning objectives 
The project has been created to expand my knowledge within the Java echo system by exploring Spring, Spring Boot, Docker and Unix-shell scripting. 
The backend is built in Java using Spring Boot. The frontend is built using shell scripts. The backend is deployed using Docker containers. 
