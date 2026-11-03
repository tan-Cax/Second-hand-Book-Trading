-- 1. user
INSERT INTO `user` (`id`, `name`, `password`, `role`, `is_banned`, `created_time`) VALUES (3,'TEST_USER_1783423742804','$2a$10$.pomg/ttWvxdsP41y6UAiuVCg..QvW93TVxvnXz/fRLnGxmkAyP3e',0,0,NULL);
INSERT INTO `user` (`id`, `name`, `password`, `role`, `is_banned`, `created_time`) VALUES (5,'TEST_USER_1783423775683','$2a$10$.ohngduVPCJz9Ncbrm8YNeRiJWzg9BlcwZNk/dCBiT8ghikClCsE.',0,0,NULL);
INSERT INTO `user` (`id`, `name`, `password`, `role`, `is_banned`, `created_time`) VALUES (7,'TEST_USER_1783426412898','$2a$10$K27lGUdTe7//mxLaieFUeuKzJvfpOOqeEjnS0Z8DGpSiePJ0lLxlO',0,0,NULL);
INSERT INTO `user` (`id`, `name`, `password`, `role`, `is_banned`, `created_time`) VALUES (9,'TEST_USER_1783427430299','$2a$10$hFrKgHMv19ys9t/XYp5SC.LL1mb5.J6RnQNkPfLNP1bNY4qo/LbwO',0,0,NULL);
INSERT INTO `user` (`id`, `name`, `password`, `role`, `is_banned`, `created_time`) VALUES (11,'test1','$2a$10$G0XUECl.0aUZMiA11MMNWeKRnRDbVwFGMO6kBV6aCX99LSRQDdVBa',0,0,NULL);
INSERT INTO `user` (`id`, `name`, `password`, `role`, `is_banned`, `created_time`) VALUES (12,'test2','$2a$10$txSE1sDwGBjXt8EYCBRCmu6/jnBxotI/H/AmeaxKwLejswUspn9sm',0,0,NULL);
INSERT INTO `user` (`id`, `name`, `password`, `role`, `is_banned`, `created_time`) VALUES (13,'test3','$2a$10$BNtB9pmgYKJMU1efE5uVVuzEzsFTdQcuVDFI4CAPuQUPoFCCZpYMa',0,0,NULL);
INSERT INTO `user` (`id`, `name`, `password`, `role`, `is_banned`, `created_time`) VALUES (14,'test4','$2a$10$zQioLnc3EVZUY9rStmoNd.VWTzbb.M6mfnvIMj7znYy47xkeHx9OO',0,0,NULL);
INSERT INTO `user` (`id`, `name`, `password`, `role`, `is_banned`, `created_time`) VALUES (15,'test5','$2a$10$BjHfjhr71YkDXAROBspY1uJQPnTbL4ULklSZu18Atk8GS9AmOApkO',0,0,NULL);
-- 2. wallet
INSERT INTO `wallet` (`id`, `user_id`, `pay_password`, `balance`, `status`, `pay_fail_count`, `pay_locked`, `created_time`) VALUES (1,11,'$2a$10$utjwBKZyYQWunZubgi1/EeWzKxfHG0.G3cODwWBC.HMTRGufZlhlC',20.00,1,0,0,'2026-07-09 20:55:04');
INSERT INTO `wallet` (`id`, `user_id`, `pay_password`, `balance`, `status`, `pay_fail_count`, `pay_locked`, `created_time`) VALUES (2,12,'$2a$10$bMF1bSd1ordHtxlffqTx5uUQn2V9rC4l9y3NOoutO.sG.KvI7I61a',100.00,1,0,0,'2026-07-09 21:16:47');
INSERT INTO `wallet` (`id`, `user_id`, `pay_password`, `balance`, `status`, `pay_fail_count`, `pay_locked`, `created_time`) VALUES (3,13,'$2a$10$Jm3quilDaMvLm9J03G8HSu1wY564FIpTL3yCmfTt8KXYx9z3BRQo2',15.00,1,0,0,'2026-07-09 21:29:07');
INSERT INTO `wallet` (`id`, `user_id`, `pay_password`, `balance`, `status`, `pay_fail_count`, `pay_locked`, `created_time`) VALUES (4,15,'$2a$10$pi88Wsy9LBdPEkRVDJkUdepjuXnfa/cqtYECsBKftZ5pxAb5uKyKO',0.00,1,0,0,'2026-07-09 21:34:05');
-- 3. book
INSERT INTO `book` (`id`, `seller_id`, `title`, `publisher`, `edition`, `author`, `description`, `price`, `quantity`, `locate`, `is_deleted`, `created_time`) VALUES (5,11,'大学物理学习指导','','第二版','','7成新',5.00,0,'良乡疏桐园',0,'2026-07-09 20:55:41');
INSERT INTO `book` (`id`, `seller_id`, `title`, `publisher`, `edition`, `author`, `description`, `price`, `quantity`, `locate`, `is_deleted`, `created_time`) VALUES (6,11,'计算理论导引','机械工业出版社','第三版','','原书第三版',8.00,2,'良乡校区博雅',0,'2026-07-09 21:16:01');
INSERT INTO `book` (`id`, `seller_id`, `title`, `publisher`, `edition`, `author`, `description`, `price`, `quantity`, `locate`, `is_deleted`, `created_time`) VALUES (7,12,'离散数学','','第二版','','',5.00,0,'良乡',0,'2026-07-09 21:17:27');
INSERT INTO `book` (`id`, `seller_id`, `title`, `publisher`, `edition`, `author`, `description`, `price`, `quantity`, `locate`, `is_deleted`, `created_time`) VALUES (8,12,'离散数学','','第三版','','',8.00,1,'良乡',0,'2026-07-09 21:17:47');
INSERT INTO `book` (`id`, `seller_id`, `title`, `publisher`, `edition`, `author`, `description`, `price`, `quantity`, `locate`, `is_deleted`, `created_time`) VALUES (9,12,'毛概','','23年版','','',5.00,1,'良乡',0,'2026-07-09 21:27:50');
INSERT INTO `book` (`id`, `seller_id`, `title`, `publisher`, `edition`, `author`, `description`, `price`, `quantity`, `locate`, `is_deleted`, `created_time`) VALUES (10,12,'面向对象编程','','','','8成新，有笔记',8.00,1,'良乡',1,'2026-07-09 21:28:35');
INSERT INTO `book` (`id`, `seller_id`, `title`, `publisher`, `edition`, `author`, `description`, `price`, `quantity`, `locate`, `is_deleted`, `created_time`) VALUES (11,13,'数学分析中的典型问题与方法','高等教育出版社','第三版','裴礼文','九成新',20.00,1,'中关村校区',0,'2026-07-09 21:30:25');
INSERT INTO `book` (`id`, `seller_id`, `title`, `publisher`, `edition`, `author`, `description`, `price`, `quantity`, `locate`, `is_deleted`, `created_time`) VALUES (12,13,'数据结构','清华大学出版社','C语言版','严薇敏','',5.00,1,'中关村校区',0,'2026-07-09 21:31:24');
INSERT INTO `book` (`id`, `seller_id`, `title`, `publisher`, `edition`, `author`, `description`, `price`, `quantity`, `locate`, `is_deleted`, `created_time`) VALUES (13,13,'数据结构题集','清华大学出版社','C语言版','严薇敏','',5.00,0,'中关村校区',0,'2026-07-09 21:32:16');
INSERT INTO `book` (`id`, `seller_id`, `title`, `publisher`, `edition`, `author`, `description`, `price`, `quantity`, `locate`, `is_deleted`, `created_time`) VALUES (14,13,'数字逻辑课本','机械工业出版社','原书第五版','','八成新',10.00,1,'中关村校区',0,'2026-07-09 21:33:40');
-- 4. book_image
INSERT INTO `book_image` (`id`, `book_id`, `image_url`, `sort_order`) VALUES (1,5,'/uploads/images/20260709_6a87f222.jpg',0);
INSERT INTO `book_image` (`id`, `book_id`, `image_url`, `sort_order`) VALUES (2,6,'/uploads/images/20260709_841d4b3d.jpg',0);
INSERT INTO `book_image` (`id`, `book_id`, `image_url`, `sort_order`) VALUES (3,7,'/uploads/images/20260709_e20b88a3.jpg',0);
INSERT INTO `book_image` (`id`, `book_id`, `image_url`, `sort_order`) VALUES (4,8,'/uploads/images/20260709_5824b8c2.jpg',0);
INSERT INTO `book_image` (`id`, `book_id`, `image_url`, `sort_order`) VALUES (5,9,'/uploads/images/20260709_6bafb0cd.jpg',0);
INSERT INTO `book_image` (`id`, `book_id`, `image_url`, `sort_order`) VALUES (6,10,'/uploads/images/20260709_fbff6f79.jpg',0);
INSERT INTO `book_image` (`id`, `book_id`, `image_url`, `sort_order`) VALUES (7,11,'/uploads/images/20260709_263e1f6c.jpg',0);
INSERT INTO `book_image` (`id`, `book_id`, `image_url`, `sort_order`) VALUES (8,12,'/uploads/images/20260709_b7d9f790.jpg',0);
INSERT INTO `book_image` (`id`, `book_id`, `image_url`, `sort_order`) VALUES (9,12,'/uploads/images/20260709_56b4df5e.jpg',1);
INSERT INTO `book_image` (`id`, `book_id`, `image_url`, `sort_order`) VALUES (10,13,'/uploads/images/20260709_003b33a9.jpg',0);
INSERT INTO `book_image` (`id`, `book_id`, `image_url`, `sort_order`) VALUES (11,13,'/uploads/images/20260709_171dd640.jpg',1);
INSERT INTO `book_image` (`id`, `book_id`, `image_url`, `sort_order`) VALUES (12,14,'/uploads/images/20260709_dac07ce8.jpg',0);
-- 5. cart
-- 6. order
INSERT INTO `order` (`id`, `user_id`, `wallet_id`, `order_time`, `total_amount`, `status`) VALUES (1,11,1,'2026-07-09 21:53:18',5.00,4);
INSERT INTO `order` (`id`, `user_id`, `wallet_id`, `order_time`, `total_amount`, `status`) VALUES (2,11,1,'2026-07-09 22:49:50',5.00,3);
INSERT INTO `order` (`id`, `user_id`, `wallet_id`, `order_time`, `total_amount`, `status`) VALUES (3,11,1,'2026-07-10 00:56:35',8.00,4);
INSERT INTO `order` (`id`, `user_id`, `wallet_id`, `order_time`, `total_amount`, `status`) VALUES (4,12,2,'2026-07-10 00:58:04',5.00,3);
INSERT INTO `order` (`id`, `user_id`, `wallet_id`, `order_time`, `total_amount`, `status`) VALUES (5,13,3,'2026-07-10 16:12:13',5.00,3);
INSERT INTO `order` (`id`, `user_id`, `wallet_id`, `order_time`, `total_amount`, `status`) VALUES (6,13,3,'2026-07-10 16:26:12',5.00,4);
INSERT INTO `order` (`id`, `user_id`, `wallet_id`, `order_time`, `total_amount`, `status`) VALUES (7,13,3,'2026-07-10 17:16:13',5.00,4);
INSERT INTO `order` (`id`, `user_id`, `wallet_id`, `order_time`, `total_amount`, `status`) VALUES (8,11,1,'2026-07-10 17:17:16',13.00,0);
-- 7. order_item
INSERT INTO `order_item` (`id`, `order_id`, `book_id`, `quantity`, `price`, `amount`) VALUES (1,1,12,1,5.00,5.00);
INSERT INTO `order_item` (`id`, `order_id`, `book_id`, `quantity`, `price`, `amount`) VALUES (2,2,13,1,5.00,5.00);
INSERT INTO `order_item` (`id`, `order_id`, `book_id`, `quantity`, `price`, `amount`) VALUES (3,3,8,1,8.00,8.00);
INSERT INTO `order_item` (`id`, `order_id`, `book_id`, `quantity`, `price`, `amount`) VALUES (4,4,5,1,5.00,5.00);
INSERT INTO `order_item` (`id`, `order_id`, `book_id`, `quantity`, `price`, `amount`) VALUES (5,5,9,1,5.00,5.00);
INSERT INTO `order_item` (`id`, `order_id`, `book_id`, `quantity`, `price`, `amount`) VALUES (6,6,9,1,5.00,5.00);
INSERT INTO `order_item` (`id`, `order_id`, `book_id`, `quantity`, `price`, `amount`) VALUES (7,7,7,1,5.00,5.00);
INSERT INTO `order_item` (`id`, `order_id`, `book_id`, `quantity`, `price`, `amount`) VALUES (8,8,7,1,5.00,5.00);
INSERT INTO `order_item` (`id`, `order_id`, `book_id`, `quantity`, `price`, `amount`) VALUES (9,8,8,1,8.00,8.00);
-- 8. frozen_amount
INSERT INTO `frozen_amount` (`id`, `order_id`, `payer_wallet_id`, `receiver_wallet_id`, `amount`, `status`, `created_at`, `updated_at`) VALUES (1,2,1,3,5.00,2,'2026-07-09 22:50:09','2026-07-09 22:51:21');
INSERT INTO `frozen_amount` (`id`, `order_id`, `payer_wallet_id`, `receiver_wallet_id`, `amount`, `status`, `created_at`, `updated_at`) VALUES (2,4,2,1,5.00,2,'2026-07-10 00:59:29','2026-07-10 01:00:02');
INSERT INTO `frozen_amount` (`id`, `order_id`, `payer_wallet_id`, `receiver_wallet_id`, `amount`, `status`, `created_at`, `updated_at`) VALUES (3,5,3,2,5.00,2,'2026-07-10 16:13:14','2026-07-10 16:14:07');
-- 9. transaction_log
INSERT INTO `transaction_log` (`id`, `wallet_id`, `amount`, `type`, `order_id`, `description`, `created_time`) VALUES (1,2,100.00,1,NULL,'用户充值 100 元','2026-07-09 21:17:03');
INSERT INTO `transaction_log` (`id`, `wallet_id`, `amount`, `type`, `order_id`, `description`, `created_time`) VALUES (2,3,15.00,1,NULL,'用户充值 15 元','2026-07-09 21:29:15');
INSERT INTO `transaction_log` (`id`, `wallet_id`, `amount`, `type`, `order_id`, `description`, `created_time`) VALUES (3,1,10.00,1,NULL,'用户充值 10 元','2026-07-09 22:50:02');
INSERT INTO `transaction_log` (`id`, `wallet_id`, `amount`, `type`, `order_id`, `description`, `created_time`) VALUES (4,1,5.00,2,2,'订单支付, orderId=2','2026-07-09 22:50:09');
INSERT INTO `transaction_log` (`id`, `wallet_id`, `amount`, `type`, `order_id`, `description`, `created_time`) VALUES (5,3,5.00,3,2,'订单确认收货, 资金入账, orderId=2','2026-07-09 22:51:21');
INSERT INTO `transaction_log` (`id`, `wallet_id`, `amount`, `type`, `order_id`, `description`, `created_time`) VALUES (6,2,5.00,2,4,'订单支付, orderId=4','2026-07-10 00:59:29');
INSERT INTO `transaction_log` (`id`, `wallet_id`, `amount`, `type`, `order_id`, `description`, `created_time`) VALUES (7,1,5.00,3,4,'订单确认收货, 资金入账, orderId=4','2026-07-10 01:00:02');
INSERT INTO `transaction_log` (`id`, `wallet_id`, `amount`, `type`, `order_id`, `description`, `created_time`) VALUES (8,3,5.00,2,5,'订单支付, orderId=5','2026-07-10 16:13:14');
INSERT INTO `transaction_log` (`id`, `wallet_id`, `amount`, `type`, `order_id`, `description`, `created_time`) VALUES (9,2,5.00,3,5,'订单确认收货, 资金入账, orderId=5','2026-07-10 16:14:07');
INSERT INTO `transaction_log` (`id`, `wallet_id`, `amount`, `type`, `order_id`, `description`, `created_time`) VALUES (10,1,10.00,1,NULL,'用户充值 10 元','2026-07-10 17:18:02');
