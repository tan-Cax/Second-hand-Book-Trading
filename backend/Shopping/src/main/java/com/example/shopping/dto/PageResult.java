package com.example.shopping.dto;

import lombok.Data;
import java.util.List;

@Data
public class PageResult<T> {
    private List<T> records;
    private int totalCount;
    private int totalPages;
    private int currentPage;
    private int pageSize;

    public static <T> PageResult<T> of(List<T> records, int totalCount, int currentPage, int pageSize) {
        PageResult<T> result = new PageResult<>();
        result.setRecords(records);
        result.setTotalCount(totalCount);
        result.setTotalPages((totalCount + pageSize - 1) / pageSize);
        result.setCurrentPage(currentPage);
        result.setPageSize(pageSize);
        return result;
    }
}
