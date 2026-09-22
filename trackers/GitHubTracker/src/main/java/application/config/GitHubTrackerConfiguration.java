package application.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@Import(DatabaseConfiguration.class)
public class GitHubTrackerConfiguration {
}

