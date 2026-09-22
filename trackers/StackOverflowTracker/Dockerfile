FROM eclipse-temurin:21-jre

WORKDIR /opt/application

COPY target/StackOverflowTracker.jar application.jar

USER 10001

ENTRYPOINT ["java", "-jar", "application.jar"]

