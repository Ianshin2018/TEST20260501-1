package com.example.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "不可空白")
        @Pattern(regexp = "^[0-9]{10,15}$", message = "格式錯誤，需為 10 到 15 碼數字")
        String phoneNumber,

        @NotBlank(message = "不可空白")
        @Size(min = 8, max = 50, message = "長度需介於 8 到 50 字元")
        String password
) {
}
