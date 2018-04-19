/*!40000 ALTER TABLE `Category` DISABLE KEYS */;
INSERT INTO `Category` (`id`, `name`, `cover`, `description`) VALUES
  (1, 'Name_1', 'Cover_1', 'Desc_1');
INSERT INTO `Category` (`id`, `name`, `cover`, `description`) VALUES
  (2, 'Name_2', 'Cover_2', 'Desc_2');
INSERT INTO `Category` (`id`, `name`, `cover`, `description`) VALUES
  (3, 'Name_3', 'Cover_3', 'Desc_3');
INSERT INTO `Category` (`id`, `name`, `cover`, `description`) VALUES
  (4, 'Name_4', 'Cover_4', 'Desc_4');
INSERT INTO `Category` (`id`, `name`, `cover`, `description`) VALUES
  (5, 'Name_5', 'Cover_5', 'Desc_5');
INSERT INTO `Category` (`id`, `name`, `cover`, `description`) VALUES
  (6, 'Name_6', 'Cover_6', 'Desc_6');
INSERT INTO `Category` (`id`, `name`, `cover`, `description`) VALUES
  (7, 'Name_7', 'Cover_7', 'Desc_7');
INSERT INTO `Category` (`id`, `name`, `cover`, `description`) VALUES
  (8, 'Name_8', 'Cover_8', 'Desc_8');
INSERT INTO `Category` (`id`, `name`, `cover`, `description`) VALUES
  (9, 'Name_9', 'Cover_9', 'Desc_9');
INSERT INTO `Category` (`id`, `name`, `cover`, `description`) VALUES
  (10, 'Name_10', 'Cover_10', 'Desc_10');
INSERT INTO `Category` (`id`, `name`, `cover`, `description`) VALUES
  (11, 'Name_11', 'Cover_11', 'Desc_11');
INSERT INTO `Category` (`id`, `name`, `cover`, `description`) VALUES
  (12, 'Name_12', 'Cover_12', 'Desc_12');
INSERT INTO `Category` (`id`, `name`, `cover`, `description`) VALUES
  (13, 'Name_13', 'Cover_13', 'Desc_13');
/*!40000 ALTER TABLE `Category` ENABLE KEYS */;

/*!40000 ALTER TABLE `CategoryTree` DISABLE KEYS */;
INSERT INTO `CategoryTree` (`ancestor`, `descendant`, `distance`) VALUES
  (0, 0, 0);
INSERT INTO `CategoryTree` (`ancestor`, `descendant`, `distance`) VALUES
  (0, 1, 1);
INSERT INTO `CategoryTree` (`ancestor`, `descendant`, `distance`) VALUES
  (1, 1, 0);
INSERT INTO `CategoryTree` (`ancestor`, `descendant`, `distance`) VALUES
  (0, 2, 2);
INSERT INTO `CategoryTree` (`ancestor`, `descendant`, `distance`) VALUES
  (1, 2, 1);
INSERT INTO `CategoryTree` (`ancestor`, `descendant`, `distance`) VALUES
  (2, 2, 0);
INSERT INTO `CategoryTree` (`ancestor`, `descendant`, `distance`) VALUES
  (0, 3, 3);
INSERT INTO `CategoryTree` (`ancestor`, `descendant`, `distance`) VALUES
  (1, 3, 2);
INSERT INTO `CategoryTree` (`ancestor`, `descendant`, `distance`) VALUES
  (2, 3, 1);
INSERT INTO `CategoryTree` (`ancestor`, `descendant`, `distance`) VALUES
  (3, 3, 0);
INSERT INTO `CategoryTree` (`ancestor`, `descendant`, `distance`) VALUES
  (0, 4, 3);
INSERT INTO `CategoryTree` (`ancestor`, `descendant`, `distance`) VALUES
  (1, 4, 2);
INSERT INTO `CategoryTree` (`ancestor`, `descendant`, `distance`) VALUES
  (2, 4, 1);
INSERT INTO `CategoryTree` (`ancestor`, `descendant`, `distance`) VALUES
  (4, 4, 0);
INSERT INTO `CategoryTree` (`ancestor`, `descendant`, `distance`) VALUES
  (0, 5, 3);
INSERT INTO `CategoryTree` (`ancestor`, `descendant`, `distance`) VALUES
  (1, 5, 2);
INSERT INTO `CategoryTree` (`ancestor`, `descendant`, `distance`) VALUES
  (2, 5, 1);
INSERT INTO `CategoryTree` (`ancestor`, `descendant`, `distance`) VALUES
  (5, 5, 0);
INSERT INTO `CategoryTree` (`ancestor`, `descendant`, `distance`) VALUES
  (0, 6, 4);
INSERT INTO `CategoryTree` (`ancestor`, `descendant`, `distance`) VALUES
  (1, 6, 3);
INSERT INTO `CategoryTree` (`ancestor`, `descendant`, `distance`) VALUES
  (2, 6, 2);
INSERT INTO `CategoryTree` (`ancestor`, `descendant`, `distance`) VALUES
  (5, 6, 1);
INSERT INTO `CategoryTree` (`ancestor`, `descendant`, `distance`) VALUES
  (6, 6, 0);
INSERT INTO `CategoryTree` (`ancestor`, `descendant`, `distance`) VALUES
  (0, 7, 4);
INSERT INTO `CategoryTree` (`ancestor`, `descendant`, `distance`) VALUES
  (1, 7, 3);
INSERT INTO `CategoryTree` (`ancestor`, `descendant`, `distance`) VALUES
  (2, 7, 2);
INSERT INTO `CategoryTree` (`ancestor`, `descendant`, `distance`) VALUES
  (5, 7, 1);
INSERT INTO `CategoryTree` (`ancestor`, `descendant`, `distance`) VALUES
  (7, 7, 0);
INSERT INTO `CategoryTree` (`ancestor`, `descendant`, `distance`) VALUES
  (0, 8, 5);
INSERT INTO `CategoryTree` (`ancestor`, `descendant`, `distance`) VALUES
  (1, 8, 4);
INSERT INTO `CategoryTree` (`ancestor`, `descendant`, `distance`) VALUES
  (2, 8, 3);
INSERT INTO `CategoryTree` (`ancestor`, `descendant`, `distance`) VALUES
  (5, 8, 2);
INSERT INTO `CategoryTree` (`ancestor`, `descendant`, `distance`) VALUES
  (6, 8, 1);
INSERT INTO `CategoryTree` (`ancestor`, `descendant`, `distance`) VALUES
  (8, 8, 0);
INSERT INTO `CategoryTree` (`ancestor`, `descendant`, `distance`) VALUES
  (0, 9, 5);
INSERT INTO `CategoryTree` (`ancestor`, `descendant`, `distance`) VALUES
  (1, 9, 4);
INSERT INTO `CategoryTree` (`ancestor`, `descendant`, `distance`) VALUES
  (2, 9, 3);
INSERT INTO `CategoryTree` (`ancestor`, `descendant`, `distance`) VALUES
  (5, 9, 2);
INSERT INTO `CategoryTree` (`ancestor`, `descendant`, `distance`) VALUES
  (7, 9, 1);
INSERT INTO `CategoryTree` (`ancestor`, `descendant`, `distance`) VALUES
  (9, 9, 0);
INSERT INTO `CategoryTree` (`ancestor`, `descendant`, `distance`) VALUES
  (0, 10, 5);
INSERT INTO `CategoryTree` (`ancestor`, `descendant`, `distance`) VALUES
  (1, 10, 4);
INSERT INTO `CategoryTree` (`ancestor`, `descendant`, `distance`) VALUES
  (2, 10, 3);
INSERT INTO `CategoryTree` (`ancestor`, `descendant`, `distance`) VALUES
  (5, 10, 2);
INSERT INTO `CategoryTree` (`ancestor`, `descendant`, `distance`) VALUES
  (7, 10, 1);
INSERT INTO `CategoryTree` (`ancestor`, `descendant`, `distance`) VALUES
  (10, 10, 0);
INSERT INTO `CategoryTree` (`ancestor`, `descendant`, `distance`) VALUES
  (0, 11, 1);
INSERT INTO `CategoryTree` (`ancestor`, `descendant`, `distance`) VALUES
  (11, 11, 0);
INSERT INTO `CategoryTree` (`ancestor`, `descendant`, `distance`) VALUES
  (0, 12, 2);
INSERT INTO `CategoryTree` (`ancestor`, `descendant`, `distance`) VALUES
  (11, 12, 1);
INSERT INTO `CategoryTree` (`ancestor`, `descendant`, `distance`) VALUES
  (12, 12, 0);
INSERT INTO `CategoryTree` (`ancestor`, `descendant`, `distance`) VALUES
  (0, 13, 2);
INSERT INTO `CategoryTree` (`ancestor`, `descendant`, `distance`) VALUES
  (11, 13, 1);
INSERT INTO `CategoryTree` (`ancestor`, `descendant`, `distance`) VALUES
  (13, 13, 0);
/*!40000 ALTER TABLE `CategoryTree` ENABLE KEYS */;
