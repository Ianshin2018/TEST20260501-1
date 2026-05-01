package com.example.library.dto;

import jakarta.validation.constraints.NotNull;

public record BorrowRequest(
        @NotNull(message = "不可空白")
        Long inventoryId
) {
}
