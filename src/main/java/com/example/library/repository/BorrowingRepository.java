package com.example.library.repository;

import com.example.library.common.exception.ApiException;
import com.example.library.dto.BorrowingResponse;
import com.example.library.dto.InventoryResponse;
import com.example.library.model.InventoryStatus;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class BorrowingRepository {

    private static final String MESSAGE_BORROW_UNAVAILABLE = "借閱失敗：此書目前不可借閱。";
    private static final String MESSAGE_INVENTORY_NOT_FOUND = "借閱失敗：查無此書籍庫存。";
    private static final String MESSAGE_RETURN_RECORD_NOT_FOUND = "還書失敗：找不到有效的借閱紀錄。";
    private static final String MESSAGE_RETURN_NOT_BORROWER = "還書失敗：只能由原借閱人歸還。";
    private static final String MESSAGE_RETURN_STATUS_INVALID = "還書失敗：此書目前不是借出狀態。";

    private final JdbcTemplate jdbcTemplate;

    public BorrowingRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<InventoryResponse> findAllInventory() {
        return jdbcTemplate.query("""
                SELECT i.inventory_id, i.isbn, b.name, b.author, i.status
                FROM inventory i
                JOIN books b ON b.isbn = i.isbn
                ORDER BY i.inventory_id
                """, (resultSet, rowNum) -> BorrowingRowMapper.mapInventory(resultSet));
    }

    public BorrowingResponse borrow(Long userId, Long inventoryId) {
        InventoryResponse inventory = findInventory(inventoryId);
        LocalDateTime borrowedAt = LocalDateTime.now();

        if (!markInventoryAsBorrowed(inventoryId)) {
            throw new ApiException(MESSAGE_BORROW_UNAVAILABLE);
        }

        createBorrowingRecord(userId, inventoryId, borrowedAt);
        return new BorrowingResponse(
                inventory.inventoryId(),
                inventory.isbn(),
                inventory.bookName(),
                InventoryStatus.BORROWED.value(),
                userId,
                borrowedAt,
                null
        );
    }

    public BorrowingResponse returnBook(Long userId, Long inventoryId) {
        InventoryResponse inventory = findInventory(inventoryId);
        BorrowingResponse activeRecord = findActiveBorrowing(inventoryId);

        if (!userId.equals(activeRecord.userId())) {
            throw new ApiException(MESSAGE_RETURN_NOT_BORROWER);
        }

        LocalDateTime returnedAt = LocalDateTime.now();
        if (!completeBorrowingRecord(userId, inventoryId, returnedAt)) {
            throw new ApiException(MESSAGE_RETURN_RECORD_NOT_FOUND);
        }
        if (!markInventoryAsAvailable(inventoryId)) {
            throw new ApiException(MESSAGE_RETURN_STATUS_INVALID);
        }

        return new BorrowingResponse(
                inventory.inventoryId(),
                inventory.isbn(),
                inventory.bookName(),
                InventoryStatus.AVAILABLE.value(),
                activeRecord.userId(),
                activeRecord.borrowingTime(),
                returnedAt
        );
    }

    private InventoryResponse findInventory(Long inventoryId) {
        List<InventoryResponse> inventories = jdbcTemplate.query("""
                SELECT i.inventory_id, i.isbn, b.name, b.author, i.status
                FROM inventory i
                JOIN books b ON b.isbn = i.isbn
                WHERE i.inventory_id = ?
                """, (resultSet, rowNum) -> BorrowingRowMapper.mapInventory(resultSet), inventoryId);

        if (inventories.isEmpty()) {
            throw new ApiException(MESSAGE_INVENTORY_NOT_FOUND);
        }
        return inventories.get(0);
    }

    private BorrowingResponse findActiveBorrowing(Long inventoryId) {
        List<BorrowingResponse> records = jdbcTemplate.query("""
                SELECT br.inventory_id, i.isbn, b.name, i.status, br.user_id, br.borrowing_time, br.return_time
                FROM borrowing_record br
                JOIN inventory i ON i.inventory_id = br.inventory_id
                JOIN books b ON b.isbn = i.isbn
                WHERE br.inventory_id = ? AND br.return_time IS NULL
                ORDER BY br.record_id DESC
                LIMIT 1
                """, (resultSet, rowNum) -> BorrowingRowMapper.mapBorrowing(resultSet), inventoryId);

        if (records.isEmpty()) {
            throw new ApiException(MESSAGE_RETURN_RECORD_NOT_FOUND);
        }
        return records.get(0);
    }

    private boolean markInventoryAsBorrowed(Long inventoryId) {
        return jdbcTemplate.update("""
                UPDATE inventory
                SET status = ?
                WHERE inventory_id = ? AND status = ?
                """, InventoryStatus.BORROWED.value(), inventoryId, InventoryStatus.AVAILABLE.value()) == 1;
    }

    private boolean markInventoryAsAvailable(Long inventoryId) {
        return jdbcTemplate.update("""
                UPDATE inventory
                SET status = ?
                WHERE inventory_id = ? AND status = ?
                """, InventoryStatus.AVAILABLE.value(), inventoryId, InventoryStatus.BORROWED.value()) == 1;
    }

    private void createBorrowingRecord(Long userId, Long inventoryId, LocalDateTime borrowedAt) {
        jdbcTemplate.update("""
                INSERT INTO borrowing_record (user_id, inventory_id, borrowing_time, return_time)
                VALUES (?, ?, ?, NULL)
                """, userId, inventoryId, Timestamp.valueOf(borrowedAt));
    }

    private boolean completeBorrowingRecord(Long userId, Long inventoryId, LocalDateTime returnedAt) {
        return jdbcTemplate.update("""
                UPDATE borrowing_record
                SET return_time = ?
                WHERE inventory_id = ? AND user_id = ? AND return_time IS NULL
                """, Timestamp.valueOf(returnedAt), inventoryId, userId) == 1;
    }
}
