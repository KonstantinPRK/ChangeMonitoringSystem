FROM eclipse-temurin:21-jre

WORKDIR /opt/application

COPY target/BotService.jar application.jar

EXPOSE 8081 9081

USER 10001

ENTRYPOINT ["java", "-jar", "application.jar"]
