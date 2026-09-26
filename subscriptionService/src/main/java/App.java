import application.config.ServiceApplicationConfiguration;

import org.springframework.boot.SpringApplication;

/**
 * Запускает приложение и передаёт управление контейнеру Spring.
 */
public class App {
    public static void main(String[] args) {
        SpringApplication.run(ServiceApplicationConfiguration.class, args);
    }
}
