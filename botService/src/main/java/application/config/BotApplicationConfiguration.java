package application.config;

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
 * Создаёт и связывает Spring-компоненты для {@code BotApplicationConfiguration}.
 */
@EnableScheduling
@SpringBootApplication(scanBasePackages = "application")
@EnableConfigurationProperties({
        BotProperties.class,
        TelegramProperties.class,
        VkProperties.class,
        SubscriptionProperties.class,
        WorkerProperties.class
})
public class BotApplicationConfiguration {
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }


    @Bean
    public HttpClient httpClient(@Qualifier("botTaskExecutor") Executor botTaskExecutor) {
        return HttpClient.newBuilder()
                .executor(botTaskExecutor)
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }


    @Bean
    public Executor botTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("bot-worker-");
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(1_000);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(45);
        executor.initialize();
        return executor;
    }


    @Bean
    public TaskScheduler botTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setThreadNamePrefix("bot-scheduler-");
        scheduler.setPoolSize(3);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(45);
        scheduler.initialize();
        return scheduler;
    }
}
