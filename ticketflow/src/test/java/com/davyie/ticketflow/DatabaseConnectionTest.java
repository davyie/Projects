package com.davyie.ticketflow;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.beans.factory.annotation.Value;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@SpringBootTest
public class DatabaseConnectionTest {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Test
    public void helloWorldTest() {
        Assertions.assertTrue(true);
    }

    @Test
    public void testConnection() {
        DataSource dataSource = DataSourceBuilder.create()
                .url(dbUrl)
                .username(dbUsername)
                .password(dbPassword)
                .build();

        Assertions.assertNotNull(dataSource);
        try (Connection connection = dataSource.getConnection()) {
            Assertions.assertNotNull(connection);
            Assertions.assertTrue(connection.isValid(10));
        } catch (SQLException e) {
            Assertions.fail("Failed to connect to the database: " + e.getMessage());
        }
    }

}
