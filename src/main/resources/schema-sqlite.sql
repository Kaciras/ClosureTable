DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS category_tree;

-- Sqlite 的自增关键字 AUTO 跟 INCREMENT 间冇得空格
CREATE TABLE category (
    id   INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT    NOT NULL
);

CREATE TABLE category_tree (
    ancestor   INTEGER  NOT NULL,
    descendant INTEGER  NOT NULL,
    distance   TINYINT  NOT NULL,
    PRIMARY KEY (descendant, ancestor, distance)
);

INSERT INTO category_tree (ancestor, descendant, distance) VALUES (0, 0, 0);
INSERT INTO category (id, name) VALUES (0, 'root');
