package com.example.shopping.dto;

import lombok.Data;
import java.math.BigDecimal;

//用于再首页上显示主要信息的dto
//不包含所有的详细信息
@Data
public class BookListDTO {
    private Integer id;
    private String title;
    private BigDecimal price;
    private String locate;
    private String imageUrl;
}
