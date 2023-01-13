# --- build stage ---
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

COPY gradlew gradlew.bat build.gradle.kts settings.gradle.kts ./
COPY gradle gradle
RUN ./gradlew --version --no-daemon

COPY src src
RUN ./gradlew bootJar --no-daemon -x test

# --- runtime stage ---
FROM eclipse-temurin:21-jre
RUN groupadd --system cauri && useradd --system --gid cauri --home-dir /app cauri
WORKDIR /app
COPY --from=build /workspace/build/libs/*.jar app.jar
RUN chown -R cauri:cauri /app

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70"
USER cauri
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
