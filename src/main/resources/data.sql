/*!40000 ALTER TABLE `category` DISABLE KEYS */;
INSERT INTO `category` (`id`, `name`, `parentId`) VALUES (1, '电子产品', 0);
INSERT INTO `category` (`id`, `name`, `parentId`) VALUES (2, '电脑配件', 1);
INSERT INTO `category` (`id`, `name`, `parentId`) VALUES (3, '硬盘', 2);
INSERT INTO `category` (`id`, `name`, `parentId`) VALUES (4, 'CPU', 2);
INSERT INTO `category` (`id`, `name`, `parentId`) VALUES (5, '显卡', 2);
INSERT INTO `category` (`id`, `name`, `parentId`) VALUES (6, 'AMD', 5);
INSERT INTO `category` (`id`, `name`, `parentId`) VALUES (7, 'NVIDIA', 5);
INSERT INTO `category` (`id`, `name`, `parentId`) VALUES (8, 'RX580', 6);
INSERT INTO `category` (`id`, `name`, `parentId`) VALUES (9, 'GTX690战术核显卡', 7);
INSERT INTO `category` (`id`, `name`, `parentId`) VALUES (10, 'RTX3080', 7);
INSERT INTO `category` (`id`, `name`, `parentId`) VALUES (11, '水果', 0);
INSERT INTO `category` (`id`, `name`, `parentId`) VALUES (12, '苹果', 1);
INSERT INTO `category` (`id`, `name`, `parentId`) VALUES (13, '西瓜', 1);
/*!40000 ALTER TABLE `category` ENABLE KEYS */;

/*!40000 ALTER TABLE `category_tree` DISABLE KEYS */;
INSERT INTO `category_tree` (`ancestor`, `descendant`, `distance`) VALUES
  (0, 1, 1);
INSERT INTO `category_tree` (`ancestor`, `descendant`, `distance`) VALUES
  (1, 1, 0);
INSERT INTO `category_tree` (`ancestor`, `descendant`, `distance`) VALUES
  (0, 2, 2);
INSERT INTO `category_tree` (`ancestor`, `descendant`, `distance`) VALUES
  (1, 2, 1);
INSERT INTO `category_tree` (`ancestor`, `descendant`, `distance`) VALUES
  (2, 2, 0);
INSERT INTO `category_tree` (`ancestor`, `descendant`, `distance`) VALUES
  (0, 3, 3);
INSERT INTO `category_tree` (`ancestor`, `descendant`, `distance`) VALUES
  (1, 3, 2);
INSERT INTO `category_tree` (`ancestor`, `descendant`, `distance`) VALUES
  (2, 3, 1);
INSERT INTO `category_tree` (`ancestor`, `descendant`, `distance`) VALUES
  (3, 3, 0);
INSERT INTO `category_tree` (`ancestor`, `descendant`, `distance`) VALUES
  (0, 4, 3);
INSERT INTO `category_tree` (`ancestor`, `descendant`, `distance`) VALUES
  (1, 4, 2);
INSERT INTO `category_tree` (`ancestor`, `descendant`, `distance`) VALUES
  (2, 4, 1);
INSERT INTO `category_tree` (`ancestor`, `descendant`, `distance`) VALUES
  (4, 4, 0);
INSERT INTO `category_tree` (`ancestor`, `descendant`, `distance`) VALUES
  (0, 5, 3);
INSERT INTO `category_tree` (`ancestor`, `descendant`, `distance`) VALUES
  (1, 5, 2);
INSERT INTO `category_tree` (`ancestor`, `descendant`, `distance`) VALUES
  (2, 5, 1);
INSERT INTO `category_tree` (`ancestor`, `descendant`, `distance`) VALUES
  (5, 5, 0);
INSERT INTO `category_tree` (`ancestor`, `descendant`, `distance`) VALUES
  (0, 6, 4);
INSERT INTO `category_tree` (`ancestor`, `descendant`, `distance`) VALUES
  (1, 6, 3);
INSERT INTO `category_tree` (`ancestor`, `descendant`, `distance`) VALUES
  (2, 6, 2);
INSERT INTO `category_tree` (`ancestor`, `descendant`, `distance`) VALUES
  (5, 6, 1);
INSERT INTO `category_tree` (`ancestor`, `descendant`, `distance`) VALUES
  (6, 6, 0);
INSERT INTO `category_tree` (`ancestor`, `descendant`, `distance`) VALUES
  (0, 7, 4);
INSERT INTO `category_tree` (`ancestor`, `descendant`, `distance`) VALUES
  (1, 7, 3);
INSERT INTO `category_tree` (`ancestor`, `descendant`, `distance`) VALUES
  (2, 7, 2);
INSERT INTO `category_tree` (`ancestor`, `descendant`, `distance`) VALUES
  (5, 7, 1);
INSERT INTO `category_tree` (`ancestor`, `descendant`, `distance`) VALUES
  (7, 7, 0);
INSERT INTO `category_tree` (`ancestor`, `descendant`, `distance`) VALUES
  (0, 8, 5);
INSERT INTO `category_tree` (`ancestor`, `descendant`, `distance`) VALUES
  (1, 8, 4);
INSERT INTO `category_tree` (`ancestor`, `descendant`, `distance`) VALUES
  (2, 8, 3);
INSERT INTO `category_tree` (`ancestor`, `descendant`, `distance`) VALUES
  (5, 8, 2);
INSERT INTO `category_tree` (`ancestor`, `descendant`, `distance`) VALUES
  (6, 8, 1);
INSERT INTO `category_tree` (`ancestor`, `descendant`, `distance`) VALUES
  (8, 8, 0);
INSERT INTO `category_tree` (`ancestor`, `descendant`, `distance`) VALUES
  (0, 9, 5);
INSERT INTO `category_tree` (`ancestor`, `descendant`, `distance`) VALUES
  (1, 9, 4);
INSERT INTO `category_tree` (`ancestor`, `descendant`, `distance`) VALUES
  (2, 9, 3);
INSERT INTO `category_tree` (`ancestor`, `descendant`, `distance`) VALUES
  (5, 9, 2);
INSERT INTO `category_tree` (`ancestor`, `descendant`, `distance`) VALUES
  (7, 9, 1);
INSERT INTO `category_tree` (`ancestor`, `descendant`, `distance`) VALUES
  (9, 9, 0);
INSERT INTO `category_tree` (`ancestor`, `descendant`, `distance`) VALUES
  (0, 10, 5);
INSERT INTO `category_tree` (`ancestor`, `descendant`, `distance`) VALUES
  (1, 10, 4);
INSERT INTO `category_tree` (`ancestor`, `descendant`, `distance`) VALUES
  (2, 10, 3);
INSERT INTO `category_tree` (`ancestor`, `descendant`, `distance`) VALUES
  (5, 10, 2);
INSERT INTO `category_tree` (`ancestor`, `descendant`, `distance`) VALUES
  (7, 10, 1);
INSERT INTO `category_tree` (`ancestor`, `descendant`, `distance`) VALUES
  (10, 10, 0);
INSERT INTO `category_tree` (`ancestor`, `descendant`, `distance`) VALUES
  (0, 11, 1);
INSERT INTO `category_tree` (`ancestor`, `descendant`, `distance`) VALUES
  (11, 11, 0);
INSERT INTO `category_tree` (`ancestor`, `descendant`, `distance`) VALUES
  (0, 12, 2);
INSERT INTO `category_tree` (`ancestor`, `descendant`, `distance`) VALUES
  (11, 12, 1);
INSERT INTO `category_tree` (`ancestor`, `descendant`, `distance`) VALUES
  (12, 12, 0);
INSERT INTO `category_tree` (`ancestor`, `descendant`, `distance`) VALUES
  (0, 13, 2);
INSERT INTO `category_tree` (`ancestor`, `descendant`, `distance`) VALUES
  (11, 13, 1);
INSERT INTO `category_tree` (`ancestor`, `descendant`, `distance`) VALUES
  (13, 13, 0);
/*!40000 ALTER TABLE `category_tree` ENABLE KEYS */;
