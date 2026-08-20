# Build context must be the repo root (not Employee/) — this is a multi-module
# Maven reactor and needs the root pom.xml alongside Employee/ to resolve the
# parent POM. e.g. docker build -f Employee/Dockerfile .

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
COPY Employee/pom.xml Employee/pom.xml
RUN mvn -q -pl Employee -am dependency:go-offline

COPY Employee/src Employee/src
RUN mvn -q -pl Employee -am package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /workspace/Employee/target/Employee-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java","-jar","app.jar"]
