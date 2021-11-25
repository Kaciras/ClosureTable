DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS category_tree;

CREATE TABLE category_tree (
    ancestor INTEGER NOT NULL,
    descendant INTEGER NOT NULL,
    distance SMALLINT NOT NULL,
    PRIMARY KEY (ancestor, descendant, distance)
);

CREATE TABLE category (
    id SERIAL NOT NULL,
    name TEXT NOT NULL,
    PRIMARY KEY (id)
);

INSERT INTO category_tree (ancestor, descendant, distance) VALUES (0, 0, 0);
INSERT INTO category (id, name) VALUES (0, 'root');
