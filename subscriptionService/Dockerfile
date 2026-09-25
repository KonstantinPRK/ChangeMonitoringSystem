FROM eclipse-temurin:21-jre

WORKDIR /opt/subscription-service

COPY --chown=10001:10001 target/SubscriptionService.jar application.jar

EXPOSE 8080 9080

USER 10001

ENTRYPOINT ["java", "-jar", "application.jar"]
