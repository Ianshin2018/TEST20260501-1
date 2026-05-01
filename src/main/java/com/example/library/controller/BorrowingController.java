package com.example.library.controller;

import com.example.library.dto.ApiResponse;
import com.example.library.dto.BorrowRequest;
import com.example.library.dto.BorrowingResponse;
import com.example.library.dto.InventoryResponse;
import com.example.library.security.AuthInterceptor;
import com.example.library.security.SessionUser;
import com.example.library.service.BorrowingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class BorrowingController {

    private final BorrowingService borrowingService;

    public BorrowingController(BorrowingService borrowingService) {
        this.borrowingService = borrowingService;
    }

    @GetMapping("/inventories")
    public ApiResponse<List<InventoryResponse>> getInventories() {
        return ApiResponse.success("查詢庫存成功。", borrowingService.getInventory());
    }

    @PostMapping("/borrowings/borrow")
    public ApiResponse<BorrowingResponse> borrow(
            @Valid @RequestBody BorrowRequest request,
            HttpServletRequest httpServletRequest
    ) {
        SessionUser sessionUser = (SessionUser) httpServletRequest.getAttribute(AuthInterceptor.AUTHENTICATED_USER);
        return ApiResponse.success("借閱成功。", borrowingService.borrow(sessionUser, request.inventoryId()));
    }

    @PostMapping("/borrowings/return")
    public ApiResponse<BorrowingResponse> returnBook(
            @Valid @RequestBody BorrowRequest request,
            HttpServletRequest httpServletRequest
    ) {
        SessionUser sessionUser = (SessionUser) httpServletRequest.getAttribute(AuthInterceptor.AUTHENTICATED_USER);
        return ApiResponse.success("還書成功。", borrowingService.returnBook(sessionUser, request.inventoryId()));
    }
}
