# ============================================================================
# DOCKERFILE FOR ORDER SERVICE
# ============================================================================
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build
RUN apk add --no-cache maven
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw mvnw.cmd ./
RUN mvn dependency:go-offline -B
COPY src ./src
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
RUN chown -R appuser:appgroup /app
USER appuser
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC -XX:+UseStringDeduplication -Djava.security.egd=file:/dev/./urandom"
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:${SERVER_PORT:-8085}/actuator/health || exit 1
EXPOSE 8085
ENTRYPOINT ["sh", "-c", "if [ -n \"$SPRING_DATASOURCE_URL\" ]; then export SPRING_DATASOURCE_URL=$(echo $SPRING_DATASOURCE_URL | sed 's|postgres://|jdbc:postgresql://|'); fi && java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
