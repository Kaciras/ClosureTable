# ClosureTable

[![Test](https://github.com/Kaciras/ClosureTable/actions/workflows/test.yml/badge.svg)](https://github.com/Kaciras/ClosureTable/actions/workflows/test.yml)

基于 ClosureTable 的数据库无限级分类实现。

![概念图](https://github.com/Kaciras/ClosureTable/blob/master/ClosureTable.png)

Closure table 以一张表存储节点之间的关系、其中包含了任何两个有关系（祖先与子代）节点的关联信息，共有 3 个字段：

* `ancestor` 祖先节点的 ID
* `descendant` 子代节点的 ID
* `distance` 子代到祖先中间隔了几代

Closure table 能够很好地解决树结构在关系数据库中的查询需求。

配套文章 [https://blog.kaciras.com/article/6/store-tree-in-database](https://blog.kaciras.com/article/6/store-tree-in-database)

# 运行演示

![screenshot](https://github.com/Kaciras/ClosureTable/blob/master/demo.png)

本项目带有一个演示网页，能够直观地展示分类结构以及各种操作对应的 SQL，运行演示前需要修改`application.properties`文件，配置数据库连接。

然后使用以下命令构建并启动：

```bash
mvn package
java -jar target/closure-table-2.0.0.jar
```

访问 [http://localhost:6666](http://localhost:6666) 查看演示页面。

* SQL见 `CategoryMapper.java`。

* 完整的 API 见`Repository.java`和`Category.java`。
