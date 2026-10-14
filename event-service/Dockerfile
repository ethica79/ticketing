# ---- Stage 1: build the jar (uses a full JDK, thrown away afterwards) ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copy the Gradle wrapper and build config first: Docker caches this layer,
# so dependencies are only re-downloaded when build.gradle changes.
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
RUN ./gradlew dependencies --no-daemon > /dev/null

# Then copy the source code and build the jar (tests are run in CI/locally, not here).
COPY src src
RUN ./gradlew bootJar --no-daemon -x test

# ---- Stage 2: the image that actually runs (only a JRE + the jar) ----
FROM eclipse-temurin:21-jre
WORKDIR /app

# Don't run as root inside the container.
RUN useradd --system --uid 1001 spring
USER spring

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
