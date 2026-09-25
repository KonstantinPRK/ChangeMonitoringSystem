package application.config;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;

@SpringBootApplication(scanBasePackages = "application")
@EntityScan("application.persistence")
@EnableScheduling
public class ServiceApplicationConfiguration {


    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
