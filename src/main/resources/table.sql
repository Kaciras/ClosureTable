CREATE TABLE `Category` (
	`id` SMALLINT(5) UNSIGNED NOT NULL AUTO_INCREMENT,
	`name` TINYTEXT NOT NULL,
	`cover` VARCHAR(60) NOT NULL,
	`description` TEXT NOT NULL,
	PRIMARY KEY (`id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB;

CREATE TABLE `CategoryTree` (
	`ancestor` SMALLINT(5) UNSIGNED NOT NULL,
	`descendant` SMALLINT(5) UNSIGNED NOT NULL,
	`distance` TINYINT(3) UNSIGNED NOT NULL,
PRIMARY KEY (descendant, ancestor, distance))
COLLATE='utf8_general_ci'
ENGINE=InnoDB;

/* 插入顶级分类 */
INSERT INTO `CategoryTree` (ancestor,descendant,distance) VALUES (0, 0, 0);