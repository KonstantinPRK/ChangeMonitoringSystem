import application.config.TrackerApplicationConfiguration;

import org.springframework.boot.SpringApplication;

public final class App {
    private App() {
    }


    public static void main(String[] args) {
        SpringApplication.run(TrackerApplicationConfiguration.class, args);
    }
}
