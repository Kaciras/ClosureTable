DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS category_tree;

-- PG 没有 AUTOINCREMENT 关键字，取而代之的是 SERIAL
CREATE TABLE category (
    id      SERIAL  NOT NULL,
    name    TEXT    NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE category_tree (
    ancestor    INTEGER     NOT NULL,
    descendant  INTEGER     NOT NULL,
    distance    SMALLINT    NOT NULL,
    PRIMARY KEY (descendant, distance, ancestor)
);

CREATE INDEX index_0 ON category_tree (ancestor, distance);

INSERT INTO category_tree (ancestor, descendant, distance) VALUES (0, 0, 0);
INSERT INTO category (id, name) VALUES (0, 'root');
