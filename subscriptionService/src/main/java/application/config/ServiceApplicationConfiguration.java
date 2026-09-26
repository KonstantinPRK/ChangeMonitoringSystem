package application.config;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;

/**
 * Создаёт и связывает Spring-компоненты для {@code ServiceApplicationConfiguration}.
 */
@SpringBootApplication(scanBasePackages = "application")
@EnableScheduling
public class ServiceApplicationConfiguration {


    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
