package infrastructure.config;

import application.ChangeMonitoringSystem;
import application.bot.BotAvailabilityMonitor;
import application.bot.BotClient;
import application.bot.BotRegistry;
import application.bot.BotRouter;
import application.core.BotManager;
import application.core.TrackerAccessPolicy;
import application.core.TrackerManager;
import application.core.TrackingCoordinator;
import application.tracker.TrackerAvailabilityMonitor;
import application.tracker.TrackerClient;
import application.tracker.TrackerRegistry;
import application.tracker.TrackerRouter;
import infrastructure.bot.http.HttpBotClient;
import infrastructure.http.SystemHttpServer;
import infrastructure.tracker.http.HttpTrackerClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;
import java.time.Clock;

@Configuration(proxyBeanMethods = false)
public class SystemConfiguration {
    @Bean
    public SystemProperties systemProperties() {
        return SystemProperties.fromEnvironment(System.getenv());
    }


    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }


    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }


    @Bean
    public HttpClient httpClient(SystemProperties properties) {
        return HttpClient.newBuilder()
            .connectTimeout(properties.outboundConnectTimeout())
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();
    }


    @Bean
    public BotRegistry botRegistry() {
        return new BotRegistry();
    }


    @Bean
    public BotRouter botRouter(BotRegistry registry, Clock clock) {
        return new BotRouter(registry, clock);
    }


    @Bean
    public BotClient botClient(
        HttpClient httpClient,
        ObjectMapper objectMapper,
        SystemProperties properties
    ) {
        return new HttpBotClient(
            httpClient,
            objectMapper,
            properties.outboundRequestTimeout()
        );
    }


    @Bean
    public BotAvailabilityMonitor botAvailabilityMonitor(
        BotRegistry registry,
        Clock clock,
        SystemProperties properties
    ) {
        return new BotAvailabilityMonitor(
            registry,
            clock,
            properties.availabilityScanInterval()
        );
    }


    @Bean
    public BotManager botManager(
        BotRegistry registry,
        BotRouter router,
        BotClient client,
        BotAvailabilityMonitor monitor,
        Clock clock,
        SystemProperties properties
    ) {
        return new BotManager(
            registry,
            router,
            client,
            monitor,
            clock,
            properties.remoteAvailabilityTimeout()
        );
    }


    @Bean
    public TrackerRegistry trackerRegistry() {
        return new TrackerRegistry();
    }


    @Bean
    public TrackerRouter trackerRouter(TrackerRegistry registry, Clock clock) {
        return new TrackerRouter(registry, clock);
    }


    @Bean
    public TrackerClient trackerClient(
        HttpClient httpClient,
        ObjectMapper objectMapper,
        SystemProperties properties
    ) {
        return new HttpTrackerClient(
            httpClient,
            objectMapper,
            properties.outboundRequestTimeout()
        );
    }


    @Bean
    public TrackerAvailabilityMonitor trackerAvailabilityMonitor(
        TrackerRegistry registry,
        Clock clock,
        SystemProperties properties
    ) {
        return new TrackerAvailabilityMonitor(
            registry,
            clock,
            properties.availabilityScanInterval()
        );
    }


    @Bean
    public TrackerManager trackerManager(
        TrackerRegistry registry,
        TrackerRouter router,
        TrackerClient client,
        TrackerAvailabilityMonitor monitor,
        Clock clock,
        SystemProperties properties
    ) {
        return new TrackerManager(
            registry,
            router,
            client,
            monitor,
            clock,
            properties.remoteAvailabilityTimeout()
        );
    }


    @Bean
    public TrackerAccessPolicy trackerAccessPolicy() {
        return new TrackerAccessPolicy();
    }


    @Bean
    public TrackingCoordinator trackingCoordinator(
        BotManager botManager,
        TrackerManager trackerManager,
        TrackerAccessPolicy accessPolicy
    ) {
        return new TrackingCoordinator(botManager, trackerManager, accessPolicy);
    }


    @Bean
    public SystemHttpServer systemHttpServer(
        SystemProperties properties,
        BotManager botManager,
        TrackerManager trackerManager,
        ObjectMapper objectMapper
    ) {
        return new SystemHttpServer(
            properties.host(),
            properties.port(),
            properties.backlog(),
            properties.requestThreads(),
            properties.shutdownDelaySeconds(),
            botManager,
            trackerManager,
            objectMapper
        );
    }


    @Bean
    public ChangeMonitoringSystem changeMonitoringSystem(
        BotManager botManager,
        TrackerManager trackerManager,
        SystemHttpServer httpServer
    ) {
        return new ChangeMonitoringSystem(botManager, trackerManager, httpServer);
    }
}
