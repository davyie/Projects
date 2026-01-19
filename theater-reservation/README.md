# Theater Reservation 
This project is a multi-module Spring Boot project which solves the problem of handling movie bookings. 
It is built using Spring Boot 4.0, Java 21, Docker and RabbitMQ. 

## How To Build 

### Software 
- Docker Desktop or Docker Engine  
- WSL or something a like 
- Java 21, JDK 21
- Maven 
- mySQL

### Build 
Run the command `git clone https://github.com/davyie/Projects.git`. 

Enter the folder `cd Projects/theater-reservation`.

Run the command `mvn spring-boot:build-image`. This will build all the necessary images from the codebase.

Start services `docker compose up -d`. 

## Requirements 
