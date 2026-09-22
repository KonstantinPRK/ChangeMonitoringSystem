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
        String host = environment.getRequiredProperty("BOT_USER_DB_HOST");
        int port = environment.getProperty("BOT_USER_DB_PORT", Integer.class, 5432);
        String database = environment.getRequiredProperty("BOT_USER_DB_NAME");
        String username = environment.getRequiredProperty("BOT_USER_DB_USER");
        String password = environment.getRequiredProperty("BOT_USER_DB_PASSWORD");
        int poolSize = environment.getProperty("BOT_USER_DB_POOL_SIZE", Integer.class, 10);

        return new DatabaseProperties(
            "jdbc:postgresql://%s:%d/%s".formatted(host, port, database),
            username,
            password,
            poolSize
        );
    }

    @Bean(destroyMethod = "close")
    public HikariDataSource dataSource(DatabaseProperties properties) {
        HikariConfig configuration = new HikariConfig();
        configuration.setPoolName("changedetection-user-database-pool");
        configuration.setJdbcUrl(properties.jdbcUrl());
        configuration.setUsername(properties.username());
        configuration.setPassword(properties.password());
        configuration.setMaximumPoolSize(properties.maximumPoolSize());
        return new HikariDataSource(configuration);
    }
}

