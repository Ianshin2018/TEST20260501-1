package com.example.library.dto;

import java.time.LocalDateTime;

public record BorrowingResponse(
        Long inventoryId,
        String isbn,
        String bookName,
        String status,
        Long userId,
        LocalDateTime borrowingTime,
        LocalDateTime returnTime
) {
}
