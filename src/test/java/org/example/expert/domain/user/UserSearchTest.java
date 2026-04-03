package org.example.expert.domain.user;

import org.example.expert.config.DotenvInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = DotenvInitializer.class)
public class UserSearchTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void 대용량_유저목록_검색_테스트() {
        String sql = "INSERT INTO users (email, password, nickname, user_role) VALUES (? ,? , ?, ?)";

        int totalCount = 5000000; // 500만 건
        int batchSize = 10000;    // 한 번에 1만 건씩 묶어서 쏘기! (DB 메모리 보호)

        for (int j = 0; j < totalCount / batchSize; j++) {
            final int batchIndex = j;
            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    int currentId = (batchIndex * batchSize) + i;
                    String nickname = "user_" + UUID.randomUUID().toString().substring(0, 8);
                    ps.setString(1, "test" + currentId + "@test.com");
                    ps.setString(2, "password");
                    ps.setString(3, nickname);
                    ps.setString(4, "USER");
                }

                @Override
                public int getBatchSize() {
                    return batchSize;
                }
            });
        }
    }
}
