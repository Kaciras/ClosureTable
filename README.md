# ClosureTable

[![Test](https://github.com/Kaciras/ClosureTable/actions/workflows/test.yml/badge.svg)](https://github.com/Kaciras/ClosureTable/actions/workflows/test.yml)
[![GitHub license](https://img.shields.io/github/license/Kaciras/ClosureTable)](https://github.com/Kaciras/ClosureTable/blob/master/LICENSE)

基于闭包表的数据库无限级分类示例。

闭包表用一张额外的表存储节点之间的关系、其中包含了任何两个有关系（祖先与子代）节点的关联信息，共有 3 个字段：

* `ancestor` 祖先节点的 ID
* `descendant` 子代节点的 ID
* `distance` 子代到祖先中间隔了几代

以这三个字段作为条件，能够很方便的应对各种操作。闭包表是牺牲空间和修改效率来换取查询效率的典型设计。

配套文章 [https://blog.kaciras.com/article/6/store-tree-in-database](https://blog.kaciras.com/article/6/store-tree-in-database)

# 运行

![screenshot](https://github.com/Kaciras/ClosureTable/blob/master/screenshot.png)

本项目带有一个演示网页，能够直观地展示分类结构以及各种操作对应的 SQL，使用以下命令构建并启动：

```bash
mvn package
java -jar target/closure-table-3.0.0.jar
```

访问 [http://localhost:6666](http://localhost:6666) 查看演示页面。

数据库支持 Sqlite、Mariadb 和 PostgreSQL，默认使用 Sqlite 的内存数据库，可以在`application.properties`里修改数据库设置。

* 测试数据和建表脚本位于 `src/main/resources` 下。
* SQL 见 `CategoryMapper.java`。
* 完整的 API 见 `Repository.java` 和 `Category.java`。
