package com.davyie.ticketflow;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class JDBCConnectionTest {
    @Autowired
    JdbcTemplate jdbc;

    @Test
    void canConnect() {
        Integer result = jdbc.queryForObject("SELECT 1", Integer.class);
        assertThat(result).isEqualTo(1);
    }
}