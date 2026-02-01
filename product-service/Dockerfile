# Use the official Java 17 image (Matches your local setup)
FROM eclipse-temurin:17-jdk

# Set working directory inside the container
WORKDIR /app

# Copy the built JAR file into the container
# This assumes you ran 'mvn package' locally first
COPY target/*.jar app.jar

# Expose port 8080 (Just for documentation, we map ports in docker-compose)
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]