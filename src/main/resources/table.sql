SET SESSION sql_mode = 'NO_AUTO_VALUE_ON_ZERO';

CREATE TABLE IF NOT EXISTS `category` (
	`id` SMALLINT(5) UNSIGNED NOT NULL AUTO_INCREMENT,
	`name` TINYTEXT NOT NULL,
	`cover` VARCHAR(60) NOT NULL,
	`description` TEXT NOT NULL,
	PRIMARY KEY (`id`)
)
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `category_tree` (
	`ancestor` SMALLINT(5) UNSIGNED NOT NULL,
	`descendant` SMALLINT(5) UNSIGNED NOT NULL,
	`distance` TINYINT(3) UNSIGNED NOT NULL,
PRIMARY KEY (descendant, ancestor, distance))
COLLATE='utf8mb4_general_ci'
ENGINE=InnoDB;

/* 插入顶级分类 */
INSERT INTO `category_tree` (ancestor,descendant,distance) VALUES (0, 0, 0);
INSERT INTO `category` (`id`, `name`, `cover`, `description`) VALUES (0, 'root', '', 'root of the category tree');
