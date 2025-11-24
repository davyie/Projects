# Use an official OpenJDK runtime as a parent image
FROM mcr.microsoft.com/openjdk/jdk:21-azurelinux

# Set the working directory inside the container
WORKDIR /app

# Copy the executable JAR file
COPY target/*.jar app.jar

# Expose port (adjust according to your app's configuration)
EXPOSE 8080

# Run the Spring Boot app
ENTRYPOINT ["java", "-jar", "app.jar"]

FROM mcr.microsoft.com/openjdk/jdk:21-azurelinux

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]