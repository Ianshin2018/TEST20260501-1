package com.example.library.repository;

import com.example.library.common.exception.ApiException;
import com.example.library.dto.AuthResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AuthRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public AuthRepository(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    public Long register(String phoneNumber, String password, String userName) {
        try {
            Long userId = jdbcTemplate.queryForObject(
                    "CALL REGISTER_USER(?, ?, ?)",
                    Long.class,
                    phoneNumber,
                    password,
                    userName
            );

            if (userId == null) {
                throw new ApiException("註冊失敗，請稍後再試。");
            }
            return userId;
        } catch (DataAccessException exception) {
            throw translateException(exception);
        }
    }

    public AuthResponse authenticate(String phoneNumber, String password) {
        try {
            String payload = jdbcTemplate.queryForObject(
                    "CALL AUTHENTICATE_USER(?, ?)",
                    String.class,
                    phoneNumber,
                    password
            );

            if (payload == null || payload.isBlank()) {
                throw new ApiException("登入失敗，請稍後再試。");
            }
            AuthResponse authResponse = objectMapper.readValue(payload, AuthResponse.class);
            return new AuthResponse(authResponse.userId(), authResponse.phoneNumber(), authResponse.userName(), null);
        } catch (DataAccessException exception) {
            throw translateException(exception);
        } catch (JsonProcessingException exception) {
            throw new ApiException("登入資料解析失敗。");
        }
    }

    private ApiException translateException(Exception exception) {
        String message = exception.getMessage();
        if (message != null) {
            if (message.contains("此手機號碼已註冊")) {
                return new ApiException("註冊失敗：此手機號碼已註冊。");
            }
            if (message.contains("手機號碼或密碼錯誤")) {
                return new ApiException("登入失敗：手機號碼或密碼錯誤。");
            }
            if (message.contains("使用者名稱不可空白")) {
                return new ApiException("註冊失敗：使用者名稱不可空白。");
            }
            if (message.contains("手機號碼不可空白")) {
                return new ApiException("註冊失敗：手機號碼不可空白。");
            }
            if (message.contains("密碼不可空白")) {
                return new ApiException("註冊失敗：密碼不可空白。");
            }
        }
        return new ApiException("系統忙碌中，請稍後再試。");
    }
}
