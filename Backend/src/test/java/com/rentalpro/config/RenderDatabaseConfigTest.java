package com.rentalpro.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RenderDatabaseConfigTest {

    @Test
    void toJdbcUrl_convertsPostgresqlScheme() {
        assertEquals(
                "jdbc:postgresql://user:pass@host:5432/rentalpro",
                RenderDatabaseConfig.toJdbcUrl("postgresql://user:pass@host:5432/rentalpro")
        );
    }

    @Test
    void toJdbcUrl_leavesJdbcUrlUnchanged() {
        String url = "jdbc:postgresql://user:pass@host:5432/rentalpro";
        assertEquals(url, RenderDatabaseConfig.toJdbcUrl(url));
    }
}
