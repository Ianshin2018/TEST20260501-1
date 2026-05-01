package com.example.library.service;

import com.example.library.dto.BorrowingResponse;
import com.example.library.dto.InventoryResponse;
import com.example.library.repository.BorrowingRepository;
import com.example.library.security.SessionUser;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BorrowingService {

    private final BorrowingRepository borrowingRepository;

    public BorrowingService(BorrowingRepository borrowingRepository) {
        this.borrowingRepository = borrowingRepository;
    }

    public List<InventoryResponse> getInventory() {
        return borrowingRepository.findAllInventory();
    }

    @Transactional
    public BorrowingResponse borrow(SessionUser sessionUser, Long inventoryId) {
        return borrowingRepository.borrow(sessionUser.userId(), inventoryId);
    }

    @Transactional
    public BorrowingResponse returnBook(SessionUser sessionUser, Long inventoryId) {
        return borrowingRepository.returnBook(sessionUser.userId(), inventoryId);
    }
}
