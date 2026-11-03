package com.example.shopping.controller;

import com.example.shopping.dto.Result;
import com.example.shopping.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/users/{userId}/unlock-pay")
    public Result<?> unlockPayAccount(@PathVariable Integer userId,
                                      HttpServletRequest request) {
        Integer adminUserId = (Integer) request.getAttribute("currentUserId");
        return adminService.unlockPayAccount(adminUserId, userId);
    }

    @PostMapping("/users/{userId}/lock-pay")
    public Result<?> lockPayAccount(@PathVariable Integer userId,
                                    HttpServletRequest request) {
        Integer adminUserId = (Integer) request.getAttribute("currentUserId");
        return adminService.lockPayAccount(adminUserId, userId);
    }

    @PostMapping("/users/{userId}/ban")
    public Result<?> banUser(@PathVariable Integer userId,
                             HttpServletRequest request) {
        Integer adminUserId = (Integer) request.getAttribute("currentUserId");
        return adminService.banUser(adminUserId, userId);
    }

    @PostMapping("/users/{userId}/unban")
    public Result<?> unbanUser(@PathVariable Integer userId,
                               HttpServletRequest request) {
        Integer adminUserId = (Integer) request.getAttribute("currentUserId");
        return adminService.unbanUser(adminUserId, userId);
    }

    @GetMapping("/users")
    public Result<?> getUsers(@RequestParam(required = false) String keyword,
                              @RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "20") int size,
                              HttpServletRequest request) {
        Integer adminUserId = (Integer) request.getAttribute("currentUserId");
        return adminService.getUsers(adminUserId, keyword, page, size);
    }

    @GetMapping("/orders")
    public Result<?> getAllOrders(@RequestParam(required = false) Integer status,
                                 @RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "20") int size,
                                 HttpServletRequest request) {
        Integer adminUserId = (Integer) request.getAttribute("currentUserId");
        return adminService.getAllOrders(adminUserId, status, page, size);
    }

    @GetMapping("/orders/{orderId}")
    public Result<?> getOrderDetail(@PathVariable Integer orderId,
                                    HttpServletRequest request) {
        Integer adminUserId = (Integer) request.getAttribute("currentUserId");
        return adminService.getOrderDetail(adminUserId, orderId);
    }

    @GetMapping("/transactions")
    public Result<?> getAllTransactions(@RequestParam(required = false) Integer walletId,
                                       @RequestParam(required = false) Integer type,
                                       @RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "20") int size,
                                       HttpServletRequest request) {
        Integer adminUserId = (Integer) request.getAttribute("currentUserId");
        return adminService.getAllTransactions(adminUserId, walletId, type, page, size);
    }

    @GetMapping("/statistics")
    public Result<?> getStatistics(HttpServletRequest request) {
        Integer adminUserId = (Integer) request.getAttribute("currentUserId");
        return adminService.getStatistics(adminUserId);
    }

    @GetMapping("/books/deleted")
    public Result<?> getDeletedBooks(HttpServletRequest request) {
        Integer adminUserId = (Integer) request.getAttribute("currentUserId");
        return adminService.getDeletedBooks(adminUserId);
    }

    @DeleteMapping("/books/{id}")
    public Result<?> permanentDeleteBook(@PathVariable Integer id,
                                         HttpServletRequest request) {
        Integer adminUserId = (Integer) request.getAttribute("currentUserId");
        return adminService.permanentDeleteBook(adminUserId, id);
    }
}
