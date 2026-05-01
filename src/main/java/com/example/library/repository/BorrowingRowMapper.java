package com.example.library.repository;

import com.example.library.dto.BorrowingResponse;
import com.example.library.dto.InventoryResponse;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

final class BorrowingRowMapper {

    private BorrowingRowMapper() {
    }

    static InventoryResponse mapInventory(ResultSet resultSet) throws SQLException {
        return new InventoryResponse(
                resultSet.getLong("inventory_id"),
                resultSet.getString("isbn"),
                resultSet.getString("name"),
                resultSet.getString("author"),
                resultSet.getString("status")
        );
    }

    static BorrowingResponse mapBorrowing(ResultSet resultSet) throws SQLException {
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
