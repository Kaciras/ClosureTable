DROP TABLE IF EXISTS adjacent;

CREATE TABLE adjacent
(
    id     INTEGER NOT NULL,
    parent INTEGER NOT NULL,
    name   TEXT    NOT NULL,
    PRIMARY KEY (parent, id)
);

-------- 下面的部分，在导入初始数据后执行 --------

CREATE INDEX idx_adjacent_id ON adjacent (id);
