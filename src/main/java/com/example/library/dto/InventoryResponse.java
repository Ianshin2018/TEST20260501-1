package com.example.library.dto;

public record InventoryResponse(
        Long inventoryId,
        String isbn,
        String bookName,
        String author,
        String status
) {
}
