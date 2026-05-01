package com.example.library.repository;

import com.example.library.common.exception.ApiException;
import com.example.library.dto.BorrowingResponse;
import com.example.library.dto.InventoryResponse;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class BorrowingRepository {

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
                """, (resultSet, rowNum) -> new InventoryResponse(
                resultSet.getLong("inventory_id"),
                resultSet.getString("isbn"),
                resultSet.getString("name"),
                resultSet.getString("author"),
                resultSet.getString("status")
        ));
    }

    public BorrowingResponse borrow(Long userId, Long inventoryId) {
        InventoryResponse inventory = findInventory(inventoryId);
        if (!"AVAILABLE".equals(inventory.status())) {
            throw new ApiException("借閱失敗：此書目前不可借閱。");
        }

        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update("""
                INSERT INTO borrowing_record (user_id, inventory_id, borrowing_time, return_time)
                VALUES (?, ?, ?, NULL)
                """, userId, inventoryId, Timestamp.valueOf(now));
        jdbcTemplate.update("UPDATE inventory SET status = 'BORROWED' WHERE inventory_id = ?", inventoryId);

        return new BorrowingResponse(
                inventory.inventoryId(),
                inventory.isbn(),
                inventory.bookName(),
                "BORROWED",
                userId,
                now,
                null
        );
    }

    public BorrowingResponse returnBook(Long userId, Long inventoryId) {
        InventoryResponse inventory = findInventory(inventoryId);
        if (!"BORROWED".equals(inventory.status())) {
            throw new ApiException("還書失敗：此書目前不是借出狀態。");
        }

        List<BorrowingResponse> records = jdbcTemplate.query("""
                SELECT br.inventory_id, i.isbn, b.name, i.status, br.user_id, br.borrowing_time, br.return_time
                FROM borrowing_record br
                JOIN inventory i ON i.inventory_id = br.inventory_id
                JOIN books b ON b.isbn = i.isbn
                WHERE br.inventory_id = ? AND br.return_time IS NULL
                ORDER BY br.record_id DESC
                LIMIT 1
                """, (resultSet, rowNum) -> mapBorrowing(resultSet), inventoryId);

        if (records.isEmpty()) {
            throw new ApiException("還書失敗：找不到有效的借閱紀錄。");
        }

        BorrowingResponse activeRecord = records.get(0);
        if (!userId.equals(activeRecord.userId())) {
            throw new ApiException("還書失敗：只能由原借閱人歸還。");
        }

        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update("""
                UPDATE borrowing_record
                SET return_time = ?
                WHERE inventory_id = ? AND user_id = ? AND return_time IS NULL
                """, Timestamp.valueOf(now), inventoryId, userId);
        jdbcTemplate.update("UPDATE inventory SET status = 'AVAILABLE' WHERE inventory_id = ?", inventoryId);

        return new BorrowingResponse(
                activeRecord.inventoryId(),
                activeRecord.isbn(),
                activeRecord.bookName(),
                "AVAILABLE",
                activeRecord.userId(),
                activeRecord.borrowingTime(),
                now
        );
    }

    private InventoryResponse findInventory(Long inventoryId) {
        List<InventoryResponse> inventories = jdbcTemplate.query("""
                SELECT i.inventory_id, i.isbn, b.name, b.author, i.status
                FROM inventory i
                JOIN books b ON b.isbn = i.isbn
                WHERE i.inventory_id = ?
                """, (resultSet, rowNum) -> new InventoryResponse(
                resultSet.getLong("inventory_id"),
                resultSet.getString("isbn"),
                resultSet.getString("name"),
                resultSet.getString("author"),
                resultSet.getString("status")
        ), inventoryId);

        if (inventories.isEmpty()) {
            throw new ApiException("借閱失敗：查無此書籍庫存。");
        }
        return inventories.get(0);
    }

    private BorrowingResponse mapBorrowing(ResultSet resultSet) throws SQLException {
        Timestamp borrowingTime = resultSet.getTimestamp("borrowing_time");
        Timestamp returnTime = resultSet.getTimestamp("return_time");
        return new BorrowingResponse(
                resultSet.getLong("inventory_id"),
                resultSet.getString("isbn"),
                resultSet.getString("name"),
                resultSet.getString("status"),
                resultSet.getLong("user_id"),
                borrowingTime == null ? null : borrowingTime.toLocalDateTime(),
                returnTime == null ? null : returnTime.toLocalDateTime()
        );
    }
}
