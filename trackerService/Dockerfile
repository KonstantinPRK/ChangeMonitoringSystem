FROM eclipse-temurin:21-jre

WORKDIR /opt/application

COPY target/TrackerService.jar application.jar

EXPOSE 8082 9082

USER 10001

ENTRYPOINT ["java", "-jar", "application.jar"]
