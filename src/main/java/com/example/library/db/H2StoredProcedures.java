package com.example.library.db;

import com.example.library.common.security.PasswordHasher;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import org.springframework.dao.DuplicateKeyException;

public final class H2StoredProcedures {

    private H2StoredProcedures() {
    }

    public static Long registerUser(Connection connection, String phoneNumber, String password, String userName)
            throws SQLException {
        String normalizedPhone = normalize(phoneNumber);
        String normalizedUserName = normalize(userName);

        if (normalizedPhone == null || normalizedPhone.isBlank()) {
            throw new IllegalArgumentException("手機號碼不可空白");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("密碼不可空白");
        }
        if (normalizedUserName == null || normalizedUserName.isBlank()) {
            throw new IllegalArgumentException("使用者名稱不可空白");
        }

        try (PreparedStatement checkStatement =
                     connection.prepareStatement("SELECT COUNT(1) FROM users WHERE phone_number = ?")) {
            checkStatement.setString(1, normalizedPhone);
            try (ResultSet resultSet = checkStatement.executeQuery()) {
                if (resultSet.next() && resultSet.getInt(1) > 0) {
                    throw new DuplicateKeyException("此手機號碼已註冊");
                }
            }
        }

        String salt = PasswordHasher.generateSalt();
        String passwordHash = PasswordHasher.hashPassword(password, salt);
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        try (PreparedStatement insertStatement = connection.prepareStatement(
                """
                INSERT INTO users (phone_number, password_hash, password_salt, user_name, registration_time, last_login_time)
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                Statement.RETURN_GENERATED_KEYS)) {
            insertStatement.setString(1, normalizedPhone);
            insertStatement.setString(2, passwordHash);
            insertStatement.setString(3, salt);
            insertStatement.setString(4, normalizedUserName);
            insertStatement.setTimestamp(5, now);
            insertStatement.setTimestamp(6, null);
            insertStatement.executeUpdate();

            try (ResultSet generatedKeys = insertStatement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                }
            }
        }

        throw new SQLException("建立使用者失敗");
    }

    public static String authenticateUser(Connection connection, String phoneNumber, String password)
            throws SQLException {
        String normalizedPhone = normalize(phoneNumber);

        try (PreparedStatement selectStatement = connection.prepareStatement(
                """
                SELECT user_id, phone_number, password_hash, password_salt, user_name
                FROM users
                WHERE phone_number = ?
                """)) {
            selectStatement.setString(1, normalizedPhone);

            try (ResultSet resultSet = selectStatement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalArgumentException("手機號碼或密碼錯誤");
                }

                long userId = resultSet.getLong("user_id");
                String storedPhone = resultSet.getString("phone_number");
                String storedHash = resultSet.getString("password_hash");
                String storedSalt = resultSet.getString("password_salt");
                String userName = resultSet.getString("user_name");

                String incomingHash = PasswordHasher.hashPassword(password, storedSalt);
                if (!storedHash.equals(incomingHash)) {
                    throw new IllegalArgumentException("手機號碼或密碼錯誤");
                }

                updateLastLogin(connection, userId);
                return toJson(userId, storedPhone, userName);
            }
        }
    }

    private static void updateLastLogin(Connection connection, long userId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE users SET last_login_time = ? WHERE user_id = ?")) {
            statement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            statement.setLong(2, userId);
            statement.executeUpdate();
        }
    }

    private static String toJson(long userId, String phoneNumber, String userName) {
        return "{\"userId\":%d,\"phoneNumber\":\"%s\",\"userName\":\"%s\",\"authToken\":null}"
                .formatted(userId, escapeJson(phoneNumber), escapeJson(userName));
    }

    private static String escapeJson(String value) {
        StringBuilder builder = new StringBuilder();
        for (char current : value.toCharArray()) {
            switch (current) {
                case '\\' -> builder.append("\\\\");
                case '"' -> builder.append("\\\"");
                case '\b' -> builder.append("\\b");
                case '\f' -> builder.append("\\f");
                case '\n' -> builder.append("\\n");
                case '\r' -> builder.append("\\r");
                case '\t' -> builder.append("\\t");
                default -> {
                    if (current < 0x20) {
                        builder.append(String.format("\\u%04x", (int) current));
                    } else {
                        builder.append(current);
                    }
                }
            }
        }
        return builder.toString();
    }

    private static String normalize(String value) {
        return value == null ? null : value.trim();
    }
}
