FROM openjdk:17-jdk-slim-buster
VOLUME /tmp

ARG JAR_FILE
COPY target/client-0.0.1-SNAPSHOT.jar client-service.jar


ENTRYPOINT ["java","-XX:+UseContainerSupport","-XX:MaxRAM=512m", "-Djava.security.egd=file:/dev/./urandom","-Dspring.clients.active=dev","-jar","/client-service.jar"]

