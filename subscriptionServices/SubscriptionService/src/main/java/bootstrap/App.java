package bootstrap;

import application.ChangeMonitoringSystem;
import infrastructure.config.SystemConfiguration;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public final class App {
    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(SystemConfiguration.class)) {

            context.registerShutdownHook();
            ChangeMonitoringSystem system = context.getBean(ChangeMonitoringSystem.class);

            try {
                system.awaitTermination();

            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();

            }

        }
    }
}
