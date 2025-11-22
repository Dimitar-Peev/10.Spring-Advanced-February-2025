# Use Java 17 as the base image
FROM container-registry.oracle.com/java/openjdk:17

# Copy the application JAR file into the container
COPY target/smart-wallet-application-*.jar app.jar

# Define how to run the application
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]