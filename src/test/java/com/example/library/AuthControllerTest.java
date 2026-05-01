package com.example.library;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:auth-test;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=file:./DB/ddl.sql",
        "spring.sql.init.data-locations=file:./DB/dml.sql"
})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRegisterWithPhoneNumber() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "phoneNumber": "0912345678",
                                  "password": "password123",
                                  "userName": "TestUser"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("註冊成功。"))
                .andExpect(jsonPath("$.data.phoneNumber").value("0912345678"))
                .andExpect(jsonPath("$.data.userName").value("TestUser"))
                .andExpect(jsonPath("$.data.authToken").isEmpty());
    }

    @Test
    void shouldRejectDuplicatePhoneNumber() throws Exception {
        String body = """
                {
                  "phoneNumber": "0987654321",
                  "password": "password123",
                  "userName": "RepeatUser"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("註冊失敗：此手機號碼已註冊。"));
    }

    @Test
    void shouldLoginWithRegisteredPhoneNumber() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "phoneNumber": "0999988877",
                                  "password": "password123",
                                  "userName": "LoginUser"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "phoneNumber": "0999988877",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("登入成功。"))
                .andExpect(jsonPath("$.data.phoneNumber").value("0999988877"))
                .andExpect(jsonPath("$.data.userName").value("LoginUser"))
                .andExpect(jsonPath("$.data.authToken").isNotEmpty());
    }

    @Test
    void shouldRejectBorrowWhenNotLoggedIn() throws Exception {
        mockMvc.perform(post("/api/borrowings/borrow")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "inventoryId": 1
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("請先登入後再進行借閱或還書。"));
    }

    @Test
    void shouldBorrowAndReturnWhenLoggedIn() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "phoneNumber": "0977000111",
                                  "password": "password123",
                                  "userName": "BorrowUser"
                                }
                                """))
                .andExpect(status().isOk());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "phoneNumber": "0977000111",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        String content = loginResult.getResponse().getContentAsString();
        String token = content.split("\"authToken\":\"")[1].split("\"")[0];

        mockMvc.perform(post("/api/borrowings/borrow")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "inventoryId": 1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("借閱成功。"))
                .andExpect(jsonPath("$.data.inventoryId").value(1))
                .andExpect(jsonPath("$.data.status").value("BORROWED"));

        mockMvc.perform(post("/api/borrowings/return")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "inventoryId": 1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("還書成功。"))
                .andExpect(jsonPath("$.data.inventoryId").value(1))
                .andExpect(jsonPath("$.data.status").value("AVAILABLE"));
    }
}
