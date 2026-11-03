package com.example.shopping.dto;

import java.math.BigDecimal;

public class BookSearchParam {
    private String keyword;
    private String locate;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String sortDirection;

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    public String getLocate() { return locate; }
    public void setLocate(String locate) { this.locate = locate; }

    public BigDecimal getMinPrice() { return minPrice; }
    public void setMinPrice(BigDecimal minPrice) { this.minPrice = minPrice; }

    public BigDecimal getMaxPrice() { return maxPrice; }
    public void setMaxPrice(BigDecimal maxPrice) { this.maxPrice = maxPrice; }

    public String getSortDirection() { return sortDirection; }
    public void setSortDirection(String sortDirection) {
        if ("ASC".equalsIgnoreCase(sortDirection) || "DESC".equalsIgnoreCase(sortDirection)) {
            this.sortDirection = sortDirection.toUpperCase();
        } else {
            this.sortDirection = null;
        }
    }
}
