FROM eclipse-temurin:25-jdk-noble as builder
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN apt-get update && apt-get install -y maven && \
    mvn clean package -DskipTests -q && \
    ls -la target/

FROM eclipse-temurin:25-jre-noble
WORKDIR /app
EXPOSE 8080

# Create non-root user
RUN useradd -m -u 1000 appuser

# Copy JAR from builder
COPY --from=builder /build/target/paw-hub-backend-*.jar app.jar

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD java -cp app.jar org.springframework.boot.loader.launch.PropertiesLauncher -Dspring.boot.health.check.enabled=true || exit 1

# Run as non-root user
USER appuser

ENTRYPOINT ["java", "-jar", "app.jar"]
