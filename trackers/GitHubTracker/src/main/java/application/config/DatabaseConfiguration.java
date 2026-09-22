package application.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration(proxyBeanMethods = false)
public class DatabaseConfiguration {
    @Bean
    public DatabaseProperties databaseProperties(Environment environment) {
        String host = environment.getRequiredProperty("GITHUB_TRACKER_DB_HOST");
        int port = environment.getProperty("GITHUB_TRACKER_DB_PORT", Integer.class, 5432);
        String database = environment.getRequiredProperty("GITHUB_TRACKER_DB_NAME");
        String username = environment.getRequiredProperty("GITHUB_TRACKER_DB_USER");
        String password = environment.getRequiredProperty("GITHUB_TRACKER_DB_PASSWORD");
        int poolSize = environment.getProperty("GITHUB_TRACKER_DB_POOL_SIZE", Integer.class, 10);

        String jdbcUrl = "jdbc:postgresql://%s:%d/%s".formatted(host, port, database);
        return new DatabaseProperties(jdbcUrl, username, password, poolSize);
    }

    @Bean(destroyMethod = "close")
    public HikariDataSource dataSource(DatabaseProperties properties) {
        HikariConfig configuration = new HikariConfig();
        configuration.setPoolName("github-tracker-database-pool");
        configuration.setJdbcUrl(properties.jdbcUrl());
        configuration.setUsername(properties.username());
        configuration.setPassword(properties.password());
        configuration.setMaximumPoolSize(properties.maximumPoolSize());
        return new HikariDataSource(configuration);
    }
}

