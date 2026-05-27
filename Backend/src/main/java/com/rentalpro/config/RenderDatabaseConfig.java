package com.rentalpro.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

/**
 * Render (and most PaaS providers) supply DATABASE_URL as {@code postgresql://...}.
 * Spring Boot requires {@code jdbc:postgresql://...}.
 */
@Configuration
@Profile("prod")
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
public class RenderDatabaseConfig {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties(
            @Value("${DATABASE_URL:}") String databaseUrl) {
        DataSourceProperties properties = new DataSourceProperties();
        
        System.out.println("=== RenderDatabaseConfig LOADED ===");
        System.out.println("DATABASE_URL present: " + (databaseUrl != null && !databaseUrl.isBlank()));
        System.out.println("DATABASE_URL value: " + (databaseUrl != null ? databaseUrl.substring(0, Math.min(50, databaseUrl.length())) + "..." : "NULL"));
        
        if (databaseUrl != null && !databaseUrl.isBlank()) {
            String jdbcUrl = toJdbcUrl(databaseUrl);
            System.out.println("Transformed JDBC URL: " + jdbcUrl.substring(0, Math.min(60, jdbcUrl.length())) + "...");
            properties.setUrl(jdbcUrl);
        } else {
            System.out.println("WARNING: DATABASE_URL is empty or null!");
        }
        properties.setDriverClassName("org.postgresql.Driver");
        return properties;
    }

    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    static String toJdbcUrl(String url) {
        if (url.startsWith("jdbc:")) {
            return url;
        }
        if (url.startsWith("postgres://")) {
            return "jdbc:postgresql://" + url.substring("postgres://".length());
        }
        if (url.startsWith("postgresql://")) {
            return "jdbc:postgresql://" + url.substring("postgresql://".length());
        }
        return url;
    }
}