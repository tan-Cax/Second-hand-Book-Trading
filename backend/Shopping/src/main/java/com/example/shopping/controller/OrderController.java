package com.example.shopping.controller;

import com.example.shopping.dto.Result;
import com.example.shopping.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/create")
    public Result<?> createOrder(@RequestBody(required = false) Map<String, List<Integer>> body,
                                HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("currentUserId");
        List<Integer> bookIds = (body != null) ? body.get("bookIds") : null;
        return orderService.createOrderFromCart(userId, bookIds);
    }

    @PostMapping("/{orderId}/pay")
    public Result<?> payOrder(@PathVariable Integer orderId,
                              @RequestBody Map<String, Object> body,
                              HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("currentUserId");
        String payPassword = (String) body.get("payPassword");
        return orderService.payOrder(userId, orderId, payPassword);
    }

    @PostMapping("/{orderId}/ship")
    public Result<?> shipOrder(@PathVariable Integer orderId,
                               HttpServletRequest request) {
        Integer sellerId = (Integer) request.getAttribute("currentUserId");
        return orderService.shipOrder(sellerId, orderId);
    }

    @PostMapping("/{orderId}/confirm")
    public Result<?> confirmReceipt(@PathVariable Integer orderId,
                                    HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("currentUserId");
        return orderService.confirmReceipt(userId, orderId);
    }

    @PostMapping("/{orderId}/cancel")
    public Result<?> cancelOrder(@PathVariable Integer orderId,
                                 HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("currentUserId");
        return orderService.cancelOrder(userId, orderId);
    }

    @GetMapping("/my")
    public Result<?> getMyOrders(@RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "10") int size,
                                 @RequestParam(required = false) Integer status,
                                 HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("currentUserId");
        return orderService.getMyOrders(userId, status, page, size);
    }

    @GetMapping("/seller")
    public Result<?> getSellerOrders(@RequestParam(defaultValue = "1") int page,
                                     @RequestParam(defaultValue = "10") int size,
                                     HttpServletRequest request) {
        Integer sellerId = (Integer) request.getAttribute("currentUserId");
        return orderService.getSellerOrders(sellerId, page, size);
    }

    @GetMapping("/{orderId}")
    public Result<?> getOrderDetail(@PathVariable Integer orderId,
                                    HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("currentUserId");
        return orderService.getOrderDetail(userId, orderId);
    }
}
