package com.davyie.ticketflow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * A JUnit 5 + Mockito test template for mocking database connections.
 * This is useful for writing fast unit tests that rely on database connections
 * without needing an actual running database.
 */
@ExtendWith(MockitoExtension.class)
public class MockDatabaseConnect {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private DatabaseMetaData metaData;

    @BeforeEach
    void setUp() throws SQLException {
        // Configure common mock behavior
        lenient().when(dataSource.getConnection()).thenReturn(connection);
        lenient().when(connection.getMetaData()).thenReturn(metaData);
    }

    @Test
    void testMockConnectionSuccess() throws SQLException {
        // Arrange
        when(connection.isValid(anyInt())).thenReturn(true);
        when(metaData.getDatabaseProductName()).thenReturn("Mock DB");

        // Act
        Connection conn = dataSource.getConnection();
        boolean isValid = conn.isValid(5);
        String dbName = conn.getMetaData().getDatabaseProductName();

        // Assert
        assertNotNull(conn);
        assertTrue(isValid);
        assertEquals("Mock DB", dbName);

        // Verify interactions
        verify(dataSource, times(1)).getConnection();
        verify(connection, times(1)).isValid(5);
    }

    @Test
    void testMockConnectionFailure() throws SQLException {
        // Arrange
        when(dataSource.getConnection()).thenThrow(new SQLException("Database connection failed"));

        // Act & Assert
        SQLException exception = assertThrows(SQLException.class, () -> {
            dataSource.getConnection();
        });

        assertEquals("Database connection failed", exception.getMessage());
    }
}
