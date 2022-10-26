CREATE TABLE adjacent
(
    id     INTEGER NOT NULL AUTOINCREMENT,
    parent INTEGER NOT NULL,
    name   TEXT    NOT NULL,
    PRIMARY KEY (parent, id)
);

WITH RECURSIVE temp(p, n) AS (
    SELECT 130100000000, "石家庄市"
    UNION
    SELECT id,name FROM adjacent, temp
    WHERE adjacent.parent=p
)
SELECT * FROM temp;