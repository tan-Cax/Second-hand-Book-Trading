package com.example.shopping.service;

import com.example.shopping.dao.BookDao;
import com.example.shopping.dao.CartDao;
import com.example.shopping.dao.UserDao;
import com.example.shopping.dto.CartDTO;
import com.example.shopping.dto.CartItemDTO;
import com.example.shopping.dto.Result;
import com.example.shopping.entity.Book;
import com.example.shopping.entity.User;
import com.example.shopping.log.LogUtil;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartService {

    private final CartDao cartDao;
    private final BookDao bookDao;
    private final UserDao userDao;

    public CartService(CartDao cartDao, BookDao bookDao, UserDao userDao) {
        this.cartDao = cartDao;
        this.bookDao = bookDao;
        this.userDao = userDao;
    }

    public Result<?> addToCart(Integer userId, Integer bookId, Integer quantity) {
        try {
            if (quantity == null || quantity <= 0) {
                return Result.fail(400, "数量必须大于0");
            }

            Book book = bookDao.findById(bookId);
            if (book == null) {
                return Result.fail(404, "书籍不存在");
            }

            User seller = userDao.findById(book.getSellerId());
            if (seller != null && seller.getIsBanned() != null && seller.getIsBanned() == 1) {
                return Result.fail(400, "该书籍的卖家已被封禁，无法加入购物车");
            }

            if (book.getQuantity() < quantity) {
                return Result.fail(400, "库存不足，当前库存: " + book.getQuantity());
            }

            int existingQty = cartDao.getCartQuantity(userId, bookId);
            if (book.getQuantity() < existingQty + quantity) {
                return Result.fail(400, "库存不足，当前库存: " + book.getQuantity() + "，购物车已有: " + existingQty);
            }

            cartDao.addToCart(userId, bookId, quantity);
            LogUtil.info("购物车", userId, "加入购物车", "bookId=" + bookId + ", quantity=" + quantity);
            return Result.ok();
        } catch (Exception e) {
            return Result.fail(500, "操作失败: " + e.getMessage());
        }
    }

    public Result<?> removeItem(Integer userId, Integer bookId) {
        try {
            cartDao.removeItem(userId, bookId);
            LogUtil.info("购物车", userId, "移除商品", "bookId=" + bookId);
            return Result.ok();
        } catch (Exception e) {
            return Result.fail(500, "操作失败: " + e.getMessage());
        }
    }

    public Result<?> updateQuantity(Integer userId, Integer bookId, Integer quantity) {
        try {
            if (quantity == null || quantity <= 0) {
                return Result.fail(400, "数量必须大于0");
            }

            Book book = bookDao.findById(bookId);
            if (book == null) {
                return Result.fail(404, "书籍不存在");
            }

            if (book.getQuantity() < quantity) {
                return Result.fail(400, "库存不足，当前库存: " + book.getQuantity());
            }

            cartDao.updateQuantity(userId, bookId, quantity);
            LogUtil.info("购物车", userId, "修改数量", "bookId=" + bookId + ", quantity=" + quantity);
            return Result.ok();
        } catch (Exception e) {
            return Result.fail(500, "操作失败: " + e.getMessage());
        }
    }

    public Result<?> getCart(Integer userId) {
        try {
            List<CartItemDTO> items = cartDao.findByUserId(userId);
            CartDTO dto = new CartDTO();
            dto.setItems(items);
            dto.setCount(items.size());
            return Result.ok(dto);
        } catch (Exception e) {
            return Result.fail(500, "查询失败: " + e.getMessage());
        }
    }

    public Result<?> clearCart(Integer userId) {
        try {
            cartDao.clearCart(userId);
            LogUtil.info("购物车", userId, "清空购物车", "购物车已清空");
            return Result.ok();
        } catch (Exception e) {
            return Result.fail(500, "操作失败: " + e.getMessage());
        }
    }
}
