FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Install Maven and git
RUN apk add --no-cache maven

# Copy the entire project
COPY . .

# Build once
RUN mvn clean package -DskipTests

# Expose application and debug ports
EXPOSE 8080 5005

# Command that allows hot reload
CMD ["mvn", "spring-boot:run", "-Dspring-boot.run.jvmArguments=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"]