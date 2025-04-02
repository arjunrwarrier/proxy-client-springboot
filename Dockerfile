# Use Eclipse Temurin (OpenJDK) base image
FROM eclipse-temurin:17-jdk

# Set the working directory
WORKDIR /app

# Copy the JAR file into the container
COPY target/client-*.jar app.jar

# Expose port 8080 (for incoming connections)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
