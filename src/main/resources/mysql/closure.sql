-- 删除上次运行时遗留的表和数据
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS category_tree;

-- 分类属性表，保存了分类的 ID 和名称，你可以添加更多的属性。
CREATE TABLE IF NOT EXISTS category
(
    id       BIGINT unsigned NOT NULL AUTO_INCREMENT,
    name     tinytext         NOT NULL,
    PRIMARY KEY (id)
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4;

-- 分类树表，存储了分类之间的关系
CREATE TABLE IF NOT EXISTS category_tree
(
    ancestor   BIGINT unsigned    NOT NULL,
    descendant BIGINT unsigned    NOT NULL,
    distance   tinyint(3) unsigned NOT NULL
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8mb4;

-- 插入根分类，让整棵树有一个单根
INSERT INTO category_tree (ancestor, descendant, distance) VALUES (0, 0, 0);
INSERT INTO category (id, name) VALUES (0, 'root');

-------- 下面的部分，在导入初始数据后执行 --------

ALTER TABLE category_tree ADD PRIMARY KEY (descendant, distance, ancestor);
CREATE INDEX index_0 ON category_tree (ancestor, distance);
