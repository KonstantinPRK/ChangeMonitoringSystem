package application.config;

import application.connector.github.GitHubProperties;
import application.connector.stackoverflow.StackOverflowProperties;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.net.http.HttpClient;
import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.Executor;

/**
 * Создаёт и связывает Spring-компоненты для {@code TrackerApplicationConfiguration}.
 */
@EnableScheduling
@SpringBootApplication(scanBasePackages = "application")
@EnableConfigurationProperties({
        TrackerProperties.class,
        GitHubProperties.class,
        StackOverflowProperties.class,
        SubscriptionServiceProperties.class,
        WorkerProperties.class,
        KafkaProperties.class
})
public class TrackerApplicationConfiguration {
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }


    @Bean
    public HttpClient httpClient(@Qualifier("trackerTaskExecutor") Executor trackerTaskExecutor) {
        return HttpClient.newBuilder()
                .executor(trackerTaskExecutor)
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }


    @Bean
    public Executor trackerTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("tracker-service-worker-");
        executor.setCorePoolSize(6);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(1_000);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(20);
        executor.initialize();
        return executor;
    }


    @Bean
    public TaskScheduler trackerTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setThreadNamePrefix("tracker-service-scheduler-");
        scheduler.setPoolSize(3);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(20);
        scheduler.initialize();
        return scheduler;
    }
}
