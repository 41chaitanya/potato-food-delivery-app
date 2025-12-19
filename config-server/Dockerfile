# ============================================================================
# DOCKERFILE FOR CONFIG SERVER
# ============================================================================
# Build context should be the root directory to access config-repo
# Usage: docker build -f config-server/Dockerfile -t config-server .
# ============================================================================
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build
RUN apk add --no-cache maven

# Copy config-server source
COPY config-server/pom.xml ./pom.xml
COPY config-server/.mvn .mvn
COPY config-server/mvnw config-server/mvnw.cmd ./
RUN mvn dependency:go-offline -B
COPY config-server/src ./src
RUN mvn clean package -DskipTests -B
RUN mkdir -p target/extracted && \
    java -Djarmode=layertools -jar target/*.jar extract --destination target/extracted

FROM eclipse-temurin:21-jre-alpine AS runtime
RUN addgroup -g 1001 -S appgroup && adduser -u 1001 -S appuser -G appgroup
WORKDIR /app
COPY --from=builder /build/target/extracted/dependencies/ ./
COPY --from=builder /build/target/extracted/spring-boot-loader/ ./
COPY --from=builder /build/target/extracted/snapshot-dependencies/ ./
COPY --from=builder /build/target/extracted/application/ ./

# Include config-repo for native profile (copied from root context)
COPY config-repo /app/config-repo

RUN chown -R appuser:appgroup /app
USER appuser
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -Djava.security.egd=file:/dev/./urandom"
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:${SERVER_PORT:-8089}/actuator/health || exit 1
EXPOSE 8089
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
