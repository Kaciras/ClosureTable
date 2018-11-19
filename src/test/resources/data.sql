/*!40000 ALTER TABLE `category` DISABLE KEYS */;
INSERT INTO `category` (`id`, `name`) VALUES (1, 'Name_1');
INSERT INTO `category` (`id`, `name`) VALUES (2, 'Name_2');
INSERT INTO `category` (`id`, `name`) VALUES (3, 'Name_3');
INSERT INTO `category` (`id`, `name`) VALUES (4, 'Name_4');
INSERT INTO `category` (`id`, `name`) VALUES (5, 'Name_5');
INSERT INTO `category` (`id`, `name`) VALUES (6, 'Name_6');
INSERT INTO `category` (`id`, `name`) VALUES (7, 'Name_7');
INSERT INTO `category` (`id`, `name`) VALUES (8, 'Name_8');
INSERT INTO `category` (`id`, `name`) VALUES (9, 'Name_9');
INSERT INTO `category` (`id`, `name`) VALUES (10, 'Name_10');
INSERT INTO `category` (`id`, `name`) VALUES (11, 'Name_11');
INSERT INTO `category` (`id`, `name`) VALUES (12, 'Name_12');
INSERT INTO `category` (`id`, `name`) VALUES (13, 'Name_13');
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
