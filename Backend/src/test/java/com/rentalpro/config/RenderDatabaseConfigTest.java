package com.rentalpro.config;

import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RenderDatabaseConfigTest {

    @Test
    void parseDatabaseUrl_extractsCredentialsAndBuildsJdbcUrl() throws URISyntaxException {
        String databaseUrl = "postgresql://rentalpro:mypassword@dpg-host.render.com:5432/rentalpro";
        
        RenderDatabaseConfig.DatabaseUrlInfo info = RenderDatabaseConfig.parseDatabaseUrl(databaseUrl);
        
        assertEquals("jdbc:postgresql://dpg-host.render.com:5432/rentalpro", info.jdbcUrl);
        assertEquals("rentalpro", info.username);
        assertEquals("mypassword", info.password);
    }

    @Test
    void parseDatabaseUrl_handlesDefaultPort() throws URISyntaxException {
        String databaseUrl = "postgresql://user:pass@localhost/testdb";
        
        RenderDatabaseConfig.DatabaseUrlInfo info = RenderDatabaseConfig.parseDatabaseUrl(databaseUrl);
        
        assertEquals("jdbc:postgresql://localhost:5432/testdb", info.jdbcUrl);
        assertEquals("user", info.username);
        assertEquals("pass", info.password);
    }

    @Test
    void parseDatabaseUrl_handlesJdbcUrlAlready() throws URISyntaxException {
        String jdbcUrl = "jdbc:postgresql://localhost:5432/testdb";
        
        RenderDatabaseConfig.DatabaseUrlInfo info = RenderDatabaseConfig.parseDatabaseUrl(jdbcUrl);
        
        assertEquals(jdbcUrl, info.jdbcUrl);
    }
}
