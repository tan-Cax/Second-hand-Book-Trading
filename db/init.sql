-- ========================================
-- 校园二手书交易系统 - 数据库初始化脚本
-- ========================================

SET NAMES utf8mb4;
SET SQL_MODE = 'STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- 1. 建库
CREATE DATABASE IF NOT EXISTS shopping_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE shopping_db;

-- ========================================
-- 2. 建表（按依赖顺序）
-- ========================================

-- 用户表（纯身份账户，不含资金字段）
CREATE TABLE user (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role TINYINT DEFAULT 0 COMMENT '0:普通用户, 1:管理员',
    is_banned TINYINT DEFAULT 0 COMMENT '0-正常，1-已封禁',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 资金账户表（1:1 对应 user）
CREATE TABLE wallet (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE COMMENT '每个用户只能有一个钱包',
    pay_password VARCHAR(255) NOT NULL,
    balance DECIMAL(10,2) DEFAULT 0.00 COMMENT '可用余额',
    status TINYINT DEFAULT 1 COMMENT '1:正常, 0:冻结',
    pay_fail_count INT DEFAULT 0 COMMENT '支付密码连续错误次数',
    pay_locked TINYINT DEFAULT 0 COMMENT '0:正常, 1:已锁定',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id)
);

-- 书籍表
CREATE TABLE book (
    id INT AUTO_INCREMENT PRIMARY KEY,
    seller_id INT NOT NULL,
    title VARCHAR(100) NOT NULL,
    publisher VARCHAR(100),
    edition VARCHAR(50),
    author VARCHAR(100),
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    locate VARCHAR(200),
    is_deleted TINYINT DEFAULT 0,
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (seller_id) REFERENCES user(id)
);

-- 书籍图片表
CREATE TABLE book_image (
    id INT AUTO_INCREMENT PRIMARY KEY,
    book_id INT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    sort_order INT DEFAULT 0,
    FOREIGN KEY (book_id) REFERENCES book(id) ON DELETE CASCADE
);

-- 购物车表
CREATE TABLE cart (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    book_id INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    added_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (book_id) REFERENCES book(id),
    UNIQUE KEY uk_user_book (user_id, book_id)
);

-- 订单表（买家支付用 wallet_id）
CREATE TABLE `order` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    wallet_id INT NOT NULL COMMENT '买家支付钱包',
    order_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10,2) NOT NULL,
    status TINYINT DEFAULT 0 COMMENT '0:未支付, 1:已支付待发货, 2:已发货待收货, 3:已完成, 4:已取消',
    FOREIGN KEY (user_id) REFERENCES user(id),
    FOREIGN KEY (wallet_id) REFERENCES wallet(id)
);

-- 订单明细表
CREATE TABLE order_item (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    book_id INT NOT NULL,
    quantity INT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES `order`(id),
    FOREIGN KEY (book_id) REFERENCES book(id)
);

-- 冻结资金记录表（逐笔记录买家付款后、确认收货前的资金冻结）
CREATE TABLE frozen_amount (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    payer_wallet_id INT NOT NULL COMMENT '买家钱包',
    receiver_wallet_id INT NOT NULL COMMENT '卖家钱包',
    amount DECIMAL(10,2) NOT NULL,
    status TINYINT DEFAULT 1 COMMENT '1:冻结中, 2:已解冻入账, 3:已退款',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES `order`(id),
    FOREIGN KEY (payer_wallet_id) REFERENCES wallet(id),
    FOREIGN KEY (receiver_wallet_id) REFERENCES wallet(id)
);

-- 资金流水表（改为按 wallet_id 记录）
CREATE TABLE transaction_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    wallet_id INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL COMMENT '正数为收入，负数为支出',
    type TINYINT NOT NULL COMMENT '1:充值, 2:购书支出, 3:售书收入, 4:退款',
    order_id INT COMMENT '关联订单号（可为空）',
    description VARCHAR(200),
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (wallet_id) REFERENCES wallet(id),
    FOREIGN KEY (order_id) REFERENCES `order`(id)
);

-- ========================================
-- 3. 索引
-- ========================================

-- book 表索引
ALTER TABLE book ADD FULLTEXT INDEX ft_title (title) WITH PARSER ngram;
CREATE INDEX idx_book_price ON book (price);
CREATE INDEX idx_book_seller ON book (seller_id);
CREATE INDEX idx_book_deleted ON book (is_deleted);

-- order 表索引
CREATE INDEX idx_order_user ON `order` (user_id);
CREATE INDEX idx_order_wallet ON `order` (wallet_id);
CREATE INDEX idx_order_time ON `order` (order_time);
CREATE INDEX idx_order_status ON `order` (status);

-- order_item 表索引
CREATE INDEX idx_order_item_order ON order_item (order_id);
CREATE INDEX idx_order_item_book ON order_item (book_id);

-- cart 表索引
CREATE INDEX idx_cart_user ON cart (user_id);
CREATE INDEX idx_cart_added ON cart (added_time);

-- frozen_amount 索引
CREATE INDEX idx_frozen_payer ON frozen_amount (payer_wallet_id);
CREATE INDEX idx_frozen_receiver ON frozen_amount (receiver_wallet_id);
CREATE INDEX idx_frozen_order ON frozen_amount (order_id);
CREATE INDEX idx_frozen_status ON frozen_amount (status);

-- transaction_log 索引
CREATE INDEX idx_log_wallet ON transaction_log (wallet_id);
CREATE INDEX idx_log_order ON transaction_log (order_id);
CREATE INDEX idx_log_type ON transaction_log (type);

-- ========================================
-- 5. 视图
-- ========================================

-- 订单详情视图
CREATE VIEW v_order_detail AS
SELECT
    o.id AS order_id,
    o.user_id,
    o.wallet_id,
    u.name AS buyer_name,
    b.title AS book_title,
    b.author,
    b.seller_id,
    oi.quantity,
    oi.price,
    oi.amount,
    o.order_time,
    o.status
FROM `order` o
JOIN user u ON o.user_id = u.id
JOIN order_item oi ON o.id = oi.order_id
JOIN book b ON oi.book_id = b.id;

-- 钱包资产视图（可用余额 + 冻结中金额 = 总资产）
CREATE VIEW v_wallet_asset AS
SELECT
    w.id AS wallet_id,
    w.user_id,
    u.name AS user_name,
    w.balance,
    COALESCE(f.frozen_total, 0) AS frozen_amount,
    (w.balance + COALESCE(f.frozen_total, 0)) AS total_assets
FROM wallet w
JOIN user u ON w.user_id = u.id
LEFT JOIN (
    SELECT payer_wallet_id, SUM(amount) AS frozen_total
    FROM frozen_amount WHERE status = 1
    GROUP BY payer_wallet_id
) f ON w.id = f.payer_wallet_id;

-- 卖家结算视图（已完成订单）
CREATE VIEW v_seller_settlement AS
SELECT
    b.seller_id,
    u.name AS seller_name,
    oi.book_id,
    b.title AS book_title,
    oi.quantity,
    oi.amount,
    o.order_time
FROM order_item oi
JOIN book b ON oi.book_id = b.id
JOIN user u ON b.seller_id = u.id
JOIN `order` o ON oi.order_id = o.id
WHERE o.status = 3;

-- ========================================
-- 6. 初始数据（密码为BCrypt哈希）
-- ========================================

-- admin123 的哈希值（由项目 Passwordutil 实时生成，确保与后端 BCrypt 算法一致）
INSERT INTO user (name, password, role)
VALUES ('admin', '$2a$10$AXClpXogi/2/L7oYItKB/.fJGreaCS3DF2VZmXp83VElrFWg9YLk2', 1);

-- user123 的哈希值
INSERT INTO user (name, password, role)
VALUES ('testuser', '$2a$10$bdl8GlCcVhCEVUBnZXHCeuL60r5PAPVkWgo9lMUX/t7NxIm3sAfjK', 0);

-- ========================================
-- 7. 权限分配
-- ========================================

-- 后端应用账号（增删改查）
CREATE USER 'shopping_backend'@'localhost' IDENTIFIED BY 'ShoppingBackendPassword3306';
GRANT SELECT, INSERT, UPDATE, DELETE ON shopping_db.* TO 'shopping_backend'@'localhost';

-- 运维人员账号（增删改查 + DDL权限）
CREATE USER 'shopping_ops'@'localhost' IDENTIFIED BY 'ShoppingOpsPassword3306';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP, INDEX ON shopping_db.* TO 'shopping_ops'@'localhost';

FLUSH PRIVILEGES;
