import application.config.ServiceApplicationConfiguration;

import org.springframework.boot.SpringApplication;

public class App {
    public static void main(String[] args) {
        SpringApplication.run(ServiceApplicationConfiguration.class, args);
    }
}
