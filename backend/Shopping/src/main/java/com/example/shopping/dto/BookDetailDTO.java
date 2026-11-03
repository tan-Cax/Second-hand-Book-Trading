package com.example.shopping.dto;

import com.example.shopping.entity.BookImage;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BookDetailDTO {
    private Integer id;
    private Integer sellerId;
    private String title;
    private String publisher;
    private String edition;
    private String author;
    private String description;
    private BigDecimal price;
    private Integer quantity;
    private String locate;
    private LocalDateTime createdTime;
    private List<BookImage> images;
}
