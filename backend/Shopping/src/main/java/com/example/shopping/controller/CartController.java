package com.example.shopping.controller;

import com.example.shopping.dto.Result;
import com.example.shopping.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/add")
    public Result<?> addToCart(@RequestBody Map<String, Object> body,
                               HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("currentUserId");
        Integer bookId = (Integer) body.get("bookId");
        Integer quantity = body.get("quantity") != null ? (Integer) body.get("quantity") : 1;
        return cartService.addToCart(userId, bookId, quantity);
    }

    @DeleteMapping("/remove/{bookId}")
    public Result<?> removeItem(@PathVariable Integer bookId,
                                HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("currentUserId");
        return cartService.removeItem(userId, bookId);
    }

    @PutMapping("/update")
    public Result<?> updateQuantity(@RequestBody Map<String, Object> body,
                                    HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("currentUserId");
        Integer bookId = (Integer) body.get("bookId");
        Integer quantity = (Integer) body.get("quantity");
        return cartService.updateQuantity(userId, bookId, quantity);
    }

    @GetMapping("/list")
    public Result<?> getCart(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("currentUserId");
        return cartService.getCart(userId);
    }

    @DeleteMapping("/clear")
    public Result<?> clearCart(HttpServletRequest request) {
        Integer userId = (Integer) request.getAttribute("currentUserId");
        return cartService.clearCart(userId);
    }
}
