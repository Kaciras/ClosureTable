# ClosureTable

[![Test](https://github.com/Kaciras/ClosureTable/actions/workflows/test.yml/badge.svg)](https://github.com/Kaciras/ClosureTable/actions/workflows/test.yml)
[![GitHub license](https://img.shields.io/github/license/Kaciras/ClosureTable)](https://github.com/Kaciras/ClosureTable/blob/master/LICENSE)

基于闭包表的数据库无限级分类示例。 闭包表是用一张额外的表存储节点之间的关系、其中包含了任何两个有关系（祖先与子代）节点的关联信息，共有 3 个字段：

* `ancestor` 祖先节点的 ID
* `descendant` 子代节点的 ID
* `distance` 子代到祖先中间隔了几代

以这三个字段作为条件，能够很方便的应对各种操作。闭包表是牺牲空间和修改效率来换取查询效率的典型设计。

配套文章 [https://blog.kaciras.com/article/6/store-tree-in-database](https://blog.kaciras.com/article/6/store-tree-in-database)

> [!WARNING]
>
> 闭包表在现代数据库中的性能不一定优于邻接表，实际使用前请自行做性能测试，本项目也带有一个[示例](#性能测试)。

## 运行演示

![screenshot](https://github.com/Kaciras/ClosureTable/blob/master/screenshot.png)

本项目带有一个演示网页，运行要求 JAVA >= 21。

构建并启动：

```bash
mvn package
java -jar target/closure-table-4.0.0.jar
```

访问 [http://localhost:7777](http://localhost:7777) 查看演示页面。

数据库支持 Sqlite、Mariadb 和 PostgreSQL，默认使用 Sqlite 的内存数据库，可以在`application.properties`里修改数据库设置。

* 测试数据和建表脚本位于 `src/main/resources` 下。
* SQL 见 `CategoryMapper.java`。
* 完整的 API 见 `Repository.java` 和 `Category.java`。

## 性能测试

本项目带有一个简单的性能测试，对比闭包表和邻接表的性能，通过以下命令运行:

```bash
java -jar target/closure-table-4.0.0.jar benchmark
```
