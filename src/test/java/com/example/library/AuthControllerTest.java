package com.example.library;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldRegisterWithPhoneNumber() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload("0912345678", "TestUser")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("註冊成功。"))
                .andExpect(jsonPath("$.data.phoneNumber").value("0912345678"))
                .andExpect(jsonPath("$.data.userName").value("TestUser"))
                .andExpect(jsonPath("$.data.authToken").isEmpty());
    }

    @Test
    void shouldRejectDuplicatePhoneNumber() throws Exception {
        String payload = registerPayload("0987654321", "RepeatUser");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("註冊失敗：此手機號碼已註冊。"));
    }

    @Test
    void shouldLoginWithRegisteredPhoneNumber() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload("0999988877", "LoginUser")))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload("0999988877")))
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
                        .content(borrowPayload(1L)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("請先登入後再進行借閱或還書。"));
    }

    @Test
    void shouldBorrowAndReturnWhenLoggedIn() throws Exception {
        String token = registerAndLogin("0977000111", "BorrowUser");

        borrowBook(token, 1L)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("借閱成功。"))
                .andExpect(jsonPath("$.data.inventoryId").value(1))
                .andExpect(jsonPath("$.data.status").value("BORROWED"));

        returnBook(token, 1L)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("還書成功。"))
                .andExpect(jsonPath("$.data.inventoryId").value(1))
                .andExpect(jsonPath("$.data.status").value("AVAILABLE"));
    }

    @Test
    void shouldAllowOneUserToBorrowMultipleBooks() throws Exception {
        String token = registerAndLogin("0977000222", "MultiBorrowUser");

        borrowBook(token, 1L)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("BORROWED"));

        borrowBook(token, 2L)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("借閱成功。"))
                .andExpect(jsonPath("$.data.inventoryId").value(2))
                .andExpect(jsonPath("$.data.status").value("BORROWED"));
    }

    @Test
    void shouldRejectBorrowingSameBookTwice() throws Exception {
        String firstUserToken = registerAndLogin("0977000333", "FirstBorrower");
        String secondUserToken = registerAndLogin("0977000444", "SecondBorrower");

        borrowBook(firstUserToken, 1L)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("BORROWED"));

        borrowBook(secondUserToken, 1L)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("借閱失敗：此書目前不可借閱。"));
    }

    private String registerAndLogin(String phoneNumber, String userName) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload(phoneNumber, userName)))
                .andExpect(status().isOk());

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload(phoneNumber)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        return root.path("data").path("authToken").asText();
    }

    private org.springframework.test.web.servlet.ResultActions borrowBook(String token, Long inventoryId) throws Exception {
        return mockMvc.perform(post("/api/borrowings/borrow")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(borrowPayload(inventoryId)));
    }

    private org.springframework.test.web.servlet.ResultActions returnBook(String token, Long inventoryId) throws Exception {
        return mockMvc.perform(post("/api/borrowings/return")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(borrowPayload(inventoryId)));
    }

    private String registerPayload(String phoneNumber, String userName) {
        return """
                {
                  "phoneNumber": "%s",
                  "password": "password123",
                  "userName": "%s"
                }
                """.formatted(phoneNumber, userName);
    }

    private String loginPayload(String phoneNumber) {
        return """
                {
                  "phoneNumber": "%s",
                  "password": "password123"
                }
                """.formatted(phoneNumber);
    }

    private String borrowPayload(Long inventoryId) {
        return """
                {
                  "inventoryId": %d
                }
                """.formatted(inventoryId);
    }
}
