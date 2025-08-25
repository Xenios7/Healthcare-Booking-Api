# ---------- Build stage ----------
FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /app

# cache deps (fast rebuilds)
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline

# build the app (and repackage to a Boot executable jar)
COPY . .
RUN mvn -q -DskipTests package
# If your jar still isn't executable, uncomment the next line to force Boot repackage:
# RUN mvn -q -DskipTests spring-boot:repackage

# ---------- Runtime stage ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# tool for healthcheck
RUN apk add --no-cache curl

# security: run as non-root
RUN addgroup -S app && adduser -S app -G app

# copy the Boot fat JAR from the build stage
COPY --from=build /app/target/*.jar /app/app.jar

USER app
EXPOSE 8080

# healthcheck against Spring Actuator
HEALTHCHECK --interval=20s --timeout=3s --start-period=20s --retries=3 \
  CMD curl -fsS http://127.0.0.1:8080/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["java","-XX:MaxRAMPercentage=75","-jar","/app/app.jar"]
