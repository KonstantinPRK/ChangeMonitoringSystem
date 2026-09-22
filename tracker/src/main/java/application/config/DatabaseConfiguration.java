package application.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration(proxyBeanMethods = false)
public class DatabaseConfiguration {
    private static final int DEFAULT_POSTGRESQL_PORT = 5432;
    private static final int DEFAULT_MAXIMUM_POOL_SIZE = 10;

    @Bean
    public DatabaseProperties databaseProperties(Environment environment) {
        String host = environment.getRequiredProperty("TRACKER_DB_HOST");
        int port = environment.getProperty(
            "TRACKER_DB_PORT",
            Integer.class,
            DEFAULT_POSTGRESQL_PORT
        );
        String database = environment.getRequiredProperty("TRACKER_DB_NAME");
        String username = environment.getRequiredProperty("TRACKER_DB_USER");
        String password = environment.getRequiredProperty("TRACKER_DB_PASSWORD");
        int maximumPoolSize = environment.getProperty(
            "TRACKER_DB_POOL_SIZE",
            Integer.class,
            DEFAULT_MAXIMUM_POOL_SIZE
        );

        String jdbcUrl = "jdbc:postgresql://%s:%d/%s".formatted(host, port, database);
        return new DatabaseProperties(jdbcUrl, username, password, maximumPoolSize);
    }

    @Bean(destroyMethod = "close")
    public HikariDataSource dataSource(DatabaseProperties properties) {
        HikariConfig configuration = new HikariConfig();
        configuration.setPoolName("tracker-database-pool");
        configuration.setJdbcUrl(properties.jdbcUrl());
        configuration.setUsername(properties.username());
        configuration.setPassword(properties.password());
        configuration.setMaximumPoolSize(properties.maximumPoolSize());
        configuration.setMinimumIdle(1);
        configuration.setConnectionTimeout(5_000);
        configuration.setValidationTimeout(3_000);
        configuration.setInitializationFailTimeout(5_000);
        configuration.addDataSourceProperty("tcpKeepAlive", "true");

        return new HikariDataSource(configuration);
    }
}
