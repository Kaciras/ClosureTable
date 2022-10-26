/*!40101 SET @OLD_CHARACTER_SET_CLIENT = @@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS = 0 */;
/*!40101 SET @OLD_SQL_MODE = @@SQL_MODE, SQL_MODE = 'NO_AUTO_VALUE_ON_ZERO' */;

-- 删除上次运行时遗留的表和数据
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS category_tree;

-- 分类属性表，保存了分类的ID和名称，你可以在这个表中添加更多的属性
CREATE TABLE IF NOT EXISTS category
(
    id       int(10) unsigned NOT NULL AUTO_INCREMENT,
    name     tinytext         NOT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 分类树表，存储了分类之间的关系
CREATE TABLE IF NOT EXISTS category_tree
(
    ancestor   int(10) unsigned    NOT NULL,
    descendant int(10) unsigned    NOT NULL,
    distance   tinyint(3) unsigned NOT NULL,
    PRIMARY KEY (descendant, distance, ancestor),
    INDEX index_0 (ancestor, distance)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 插入根分类，让整棵树有一个单根
INSERT INTO category_tree (ancestor, descendant, distance) VALUES (0, 0, 0);
INSERT INTO category (id, name) VALUES (0, 'root');

/*!40101 SET SQL_MODE = IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS = IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT = @OLD_CHARACTER_SET_CLIENT */;
