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
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Render (and most PaaS providers) supply DATABASE_URL as {@code postgresql://user:pass@host:port/db}.
 * Spring Boot JDBC requires URL without credentials and separate username/password properties.
 * This config parses the DATABASE_URL and sets up the DataSource correctly.
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
        
        if (databaseUrl != null && !databaseUrl.isBlank()) {
            try {
                DatabaseUrlInfo info = parseDatabaseUrl(databaseUrl);
                System.out.println("Parsed JDBC URL: " + info.jdbcUrl);
                System.out.println("Parsed Username: " + info.username);
                System.out.println("Password present: " + (info.password != null && !info.password.isEmpty()));
                
                properties.setUrl(info.jdbcUrl);
                properties.setUsername(info.username);
                properties.setPassword(info.password);
            } catch (Exception e) {
                System.err.println("ERROR parsing DATABASE_URL: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Failed to parse DATABASE_URL", e);
            }
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

    /**
     * Parse DATABASE_URL in format: postgresql://user:pass@host:port/database
     * Returns JDBC URL without credentials: jdbc:postgresql://host:port/database
     */
    static DatabaseUrlInfo parseDatabaseUrl(String databaseUrl) throws URISyntaxException {
        // Handle jdbc: prefix if already present
        if (databaseUrl.startsWith("jdbc:")) {
            return new DatabaseUrlInfo(databaseUrl, null, null);
        }
        
        // Parse the URL
        URI uri = new URI(databaseUrl);
        
        String scheme = uri.getScheme();
        String host = uri.getHost();
        int port = uri.getPort() > 0 ? uri.getPort() : 5432; // Default PostgreSQL port
        String database = uri.getPath();
        if (database != null && database.startsWith("/")) {
            database = database.substring(1);
        }
        
        // Extract username and password from userInfo
        String username = null;
        String password = null;
        String userInfo = uri.getUserInfo();
        if (userInfo != null) {
            String[] parts = userInfo.split(":", 2);
            username = parts[0];
            if (parts.length > 1) {
                password = parts[1];
            }
        }
        
        // Build JDBC URL without credentials
        String jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
        
        return new DatabaseUrlInfo(jdbcUrl, username, password);
    }
    
    static class DatabaseUrlInfo {
        final String jdbcUrl;
        final String username;
        final String password;
        
        DatabaseUrlInfo(String jdbcUrl, String username, String password) {
            this.jdbcUrl = jdbcUrl;
            this.username = username;
            this.password = password;
        }
    }
}