package com.example.shopping.dao;

import com.example.shopping.ShoppingApplication;
import com.example.shopping.dto.BookListDTO;
import com.example.shopping.dto.SellerBookDTO;
import com.example.shopping.dto.BookSearchParam;
import com.example.shopping.entity.Book;
import com.example.shopping.utils.DButil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {ShoppingApplication.class, BookDaoTest.TestConfig.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BookDaoTest {

    @Configuration
    static class TestConfig {
        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    private final BookDao bookDao = new BookDao();
    private static Integer bookId;
    private static final String PREFIX = "TEST_BOOK_";
    private static final String TITLE = PREFIX + System.currentTimeMillis();

    @AfterAll
    static void cleanUpAfterAll() throws SQLException {
        String sql = "DELETE FROM book_image WHERE book_id IN (SELECT id FROM book WHERE title LIKE ?)";
        try (Connection conn = DButil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, PREFIX + "%");
            pstmt.executeUpdate();
        }
        sql = "DELETE FROM book WHERE title LIKE ?";
        try (Connection conn = DButil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, PREFIX + "%");
            pstmt.executeUpdate();
        }
    }

    @Test
    @Order(1)
    void testInsertAndFindById() throws SQLException {
        Book book = new Book();
        book.setSellerId(2);
        book.setTitle(TITLE);
        book.setAuthor("Test Author");
        book.setPublisher("Test Publisher");
        book.setEdition("1st Edition");
        book.setDescription("Test Description");
        book.setPrice(new BigDecimal("29.99"));
        book.setQuantity(10);
        book.setLocate("Library A");

        int id = bookDao.insert(book);
        assertTrue(id > 0, "Insert should return generated ID");
        bookId = book.getId();

        Book found = bookDao.findById(bookId);
        assertNotNull(found, "Book should be found by ID");
        assertEquals(TITLE, found.getTitle());
        assertEquals("Test Author", found.getAuthor());
        assertEquals(0, found.getIsDeleted().intValue());
    }

    @Test
    @Order(2)
    void testFindByIdNotFound() throws SQLException {
        Book found = bookDao.findById(-999);
        assertNull(found, "Non-existent ID should return null");
    }

    @Test
    @Order(3)
    void testFindBySellerId() throws SQLException {
        List<SellerBookDTO> books = bookDao.findBySellerId(2);
        assertNotNull(books);
        boolean found = books.stream().anyMatch(b -> TITLE.equals(b.getTitle()));
        assertTrue(found, "Inserted book should appear in seller's list");
    }

    @Test
    @Order(4)
    void testDeleteById() throws SQLException {
        assertNotNull(bookId, "bookId must be set from testInsertAndFindById");
        int rows = bookDao.deleteById(bookId);
        assertEquals(1, rows, "Delete should affect 1 row");

        Book deleted = bookDao.findById(bookId);
        assertNull(deleted, "Deleted book should not be found by findById");

        Book foundIncludingDeleted = bookDao.findByIdIncludingDeleted(bookId);
        assertNotNull(foundIncludingDeleted, "Deleted book should still exist in DB");
        assertEquals(1, foundIncludingDeleted.getIsDeleted().intValue(), "is_deleted should be 1");
    }

    @Test
    @Order(5)
    void testFindAllBooks() throws SQLException {
        List<BookListDTO> books = bookDao.findAllBooks();
        assertNotNull(books);
    }

    @Test
    @Order(6)
    void testSearchByKeyword() throws SQLException {
        List<BookListDTO> results = bookDao.searchByKeyword("Test");
        assertNotNull(results);
    }

    @Test
    @Order(7)
    void testSearchByKeywordSpecialChars() throws SQLException {
        List<BookListDTO> results = bookDao.searchByKeyword("%");
        assertNotNull(results);
    }

    @Test
    @Order(8)
    void testSearchBooksDefaultSort() throws SQLException {
        BookSearchParam param = new BookSearchParam();
        param.setKeyword("Test");
        List<BookListDTO> results = bookDao.searchBooks(param);
        assertNotNull(results);
    }

    @Test
    @Order(9)
    void testSearchBooksSortAsc() throws SQLException {
        BookSearchParam param = new BookSearchParam();
        param.setSortDirection("ASC");
        List<BookListDTO> results = bookDao.searchBooks(param);
        assertNotNull(results);
        for (int i = 1; i < results.size(); i++) {
            assertTrue(results.get(i - 1).getPrice().compareTo(results.get(i).getPrice()) <= 0,
                    "Should be sorted by price ASC");
        }
    }

    @Test
    @Order(10)
    void testSearchBooksSortDesc() throws SQLException {
        BookSearchParam param = new BookSearchParam();
        param.setSortDirection("DESC");
        List<BookListDTO> results = bookDao.searchBooks(param);
        assertNotNull(results);
        for (int i = 1; i < results.size(); i++) {
            assertTrue(results.get(i - 1).getPrice().compareTo(results.get(i).getPrice()) >= 0,
                    "Should be sorted by price DESC");
        }
    }

    @Test
    @Order(11)
    void testSearchBooksInvalidSortFallsBack() throws SQLException {
        BookSearchParam param = new BookSearchParam();
        param.setSortDirection("INVALID");
        List<BookListDTO> results = bookDao.searchBooks(param);
        assertNotNull(results, "Invalid sort should fall back to default sorting");
    }

    @Test
    @Order(12)
    void testSearchBooksWithAllParams() throws SQLException {
        BookSearchParam param = new BookSearchParam();
        param.setKeyword("Test");
        param.setLocate("Library A");
        param.setMinPrice(new BigDecimal("10"));
        param.setMaxPrice(new BigDecimal("100"));
        param.setSortDirection("ASC");
        List<BookListDTO> results = bookDao.searchBooks(param);
        assertNotNull(results);
    }

    @Test
    @Order(13)
    void testFindByLocate() throws SQLException {
        List<BookListDTO> results = bookDao.findByLocate("Library A");
        assertNotNull(results);
    }

    @Test
    @Order(14)
    void testFindByLocateNoMatch() throws SQLException {
        List<BookListDTO> results = bookDao.findByLocate("NonExistentLocateXYZ");
        assertTrue(results.isEmpty(), "No match should return empty list");
    }

    @Test
    @Order(15)
    void testFindRandomBooks() throws SQLException {
        List<BookListDTO> results = bookDao.findRandomBooks(3);
        assertNotNull(results);
        assertTrue(results.size() <= 3);
    }

    @Test
    @Order(16)
    void testFindDeletedBooks() throws SQLException {
        List<Book> deleted = bookDao.findDeletedBooks();
        assertNotNull(deleted);
        for (Book b : deleted) {
            assertEquals(1, b.getIsDeleted().intValue(), "Should only contain deleted books");
        }
    }
}
