DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS category_tree;

-- PG 没有 AUTOINCREMENT 关键字，取而代之的是 SERIAL
CREATE TABLE category (
    id      BIGSERIAL  NOT NULL,
    name    TEXT    NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE category_tree (
    ancestor    BIGINT     NOT NULL,
    descendant  BIGINT     NOT NULL,
    distance    SMALLINT    NOT NULL,
    PRIMARY KEY (descendant, distance, ancestor)
);

INSERT INTO category_tree (ancestor, descendant, distance) VALUES (0, 0, 0);
INSERT INTO category (id, name) VALUES (0, 'root');

-------- 下面的部分，在导入初始数据后执行 --------

CREATE INDEX index_0 ON category_tree (ancestor, distance);

-- PG 如果指定了 id 自增记录就不会增加，需要手动修复，这一点很不人性化。
SELECT setval('category_id_seq', (SELECT MAX (id) FROM category) + 1);
