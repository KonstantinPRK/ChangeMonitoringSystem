FROM eclipse-temurin:21-jre

WORKDIR /opt/application

COPY target/ChangeDetectionBot.jar application.jar

USER 10001

ENTRYPOINT ["java", "-jar", "application.jar"]

