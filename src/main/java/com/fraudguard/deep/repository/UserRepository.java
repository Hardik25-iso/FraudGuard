package com.fraudguard.deep.repository;

import com.fraudguard.hardik.model.user.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {
    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public User findByUsername(String username) {
        return jdbcTemplate.query(
                "SELECT * FROM users WHERE username = ?",
                rs -> {
                    if (rs.next()) {
                        return new User(
                                rs.getLong("id"),
                                rs.getString("username"),
                                rs.getString("password"),
                                rs.getString("full_name"),
                                rs.getString("role"),
                                rs.getString("account_id")
                        );
                    }
                    return null;
                },
                username
        );
    }

    public void save(String username, String password, String fullName, String role, String accountId) {
        jdbcTemplate.update(
                "INSERT INTO users (username, password, full_name, role, account_id) VALUES (?, ?, ?, ?, ?)",
                username, password, fullName, role, accountId
        );
    }
}
