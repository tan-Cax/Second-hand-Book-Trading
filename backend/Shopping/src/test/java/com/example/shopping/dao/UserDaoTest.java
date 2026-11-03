package com.example.shopping.dao;

import com.example.shopping.ShoppingApplication;
import com.example.shopping.entity.User;
import com.example.shopping.utils.Passwordutil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {ShoppingApplication.class, UserDaoTest.TestConfig.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserDaoTest {

    @Configuration
    static class TestConfig {
        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    private final UserDao userDao = new UserDao();
    private static Integer userId;
    private static final String PREFIX = "TEST_USER_";
    private static final String USERNAME = PREFIX + System.currentTimeMillis();
    private static final String PASSWORD = "testPass123";

    @Test
    @Order(1)
    void testInsert() throws SQLException {
        User user = new User();
        user.setName(USERNAME);
        user.setPassword(Passwordutil.encode(PASSWORD));
        user.setRole(0);
        int id = userDao.insert(user);
        assertTrue(id > 0, "Insert should return generated ID");
        userId = user.getId();
        assertNotNull(userId);
    }

    @Test
    @Order(2)
    void testFindById() throws SQLException {
        assertNotNull(userId);
        User user = userDao.findById(userId);
        assertNotNull(user, "User should be found by ID");
        assertEquals(USERNAME, user.getName());
    }

    @Test
    @Order(3)
    void testFindByName() throws SQLException {
        User user = userDao.findByName(USERNAME);
        assertNotNull(user, "User should be found by name");
        assertEquals(userId, user.getId());
        assertTrue(Passwordutil.matches(PASSWORD, user.getPassword()),
                "Password should match encoded hash");
    }

    @Test
    @Order(4)
    void testFindByNameNotFound() throws SQLException {
        User user = userDao.findByName("non_existent_user_xyz_123");
        assertNull(user, "Non-existent name should return null");
    }

    @Test
    @Order(5)
    void testInsertDuplicateName() {
        User user = new User();
        user.setName(USERNAME);
        user.setPassword("somePass");
        user.setRole(0);
        assertThrows(SQLException.class, () -> userDao.insert(user),
                "Duplicate name should throw SQLException");
    }

    @Test
    @Order(6)
    void testUpdatePassword() throws SQLException {
        assertNotNull(userId);
        String newPassword = Passwordutil.encode("newPass456");
        int rows = userDao.updatePassword(userId, newPassword);
        assertEquals(1, rows, "Update should affect 1 row");

        User user = userDao.findById(userId);
        assertNotNull(user);
        assertTrue(Passwordutil.matches("newPass456", user.getPassword()),
                "Password should be updated");
    }

    @Test
    @Order(7)
    void testFindAllPagination() throws SQLException {
        List<User> users = userDao.findAll(null, 1, 10);
        assertNotNull(users);
        assertFalse(users.isEmpty(), "Should return at least the seed users");
    }

    @Test
    @Order(8)
    void testFindAllWithKeyword() throws SQLException {
        List<User> users = userDao.findAll("admin", 1, 10);
        assertNotNull(users);
        boolean found = users.stream().anyMatch(u -> u.getName().contains("admin"));
        assertTrue(found, "Should find user matching keyword");
    }

    @Test
    @Order(9)
    void testFindAllPageZero() throws SQLException {
        List<User> users = userDao.findAll(null, 0, 10);
        assertNotNull(users);
    }

    @Test
    @Order(10)
    void testCountAll() throws SQLException {
        int count = userDao.countAll();
        assertTrue(count >= 2, "Should have at least 2 seed users");
    }

    @Test
    @Order(11)
    void testCountByKeyword() throws SQLException {
        int count = userDao.countByKeyword("admin");
        assertTrue(count >= 1);
    }

    @Test
    @Order(12)
    void testFindByIdNotFound() throws SQLException {
        User user = userDao.findById(-999);
        assertNull(user);
    }
}
