# Use an official Maven image with Java 17
FROM maven:3.9-eclipse-temurin-17

# Set the working directory inside the container
WORKDIR /app

# For development, we just want to keep the container alive so we can mount our code to it later
CMD ["tail", "-f", "/dev/null"]