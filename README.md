# ClosureTable

[![Test](https://github.com/Kaciras/ClosureTableCateogryStore/actions/workflows/test.yml/badge.svg)](https://github.com/Kaciras/ClosureTableCateogryStore/actions/workflows/test.yml)

基于ClosureTable的数据库无限级分类存储实现。

![概念图](https://github.com/Kaciras/ClosureTableCateogryStore/blob/master/ClosureTable.png)

Closure table 以一张表存储节点之间的关系、其中包含了任何两个有关系（祖先与子代）节点的关联信息。其包含3个字段：
                                                     
* `ancestor` 祖先：祖先节点的id
* `descendant` 子代：子代节点的id
* `distance` 距离：子代到祖先中间隔了几代

Closure table 能够很好地解决树结构在关系数据库中的查询需求。

配套文章 [https://blog.kaciras.com/article/6/store-tree-in-database](https://blog.kaciras.com/article/6/store-tree-in-database)

# 运行演示

运行需要配置 MySQL 或 Mariadb 数据库连接，请先修改`application.properties`文件。

然后运行以下命令构建并启动：

```bash
mvn package
java -jar target/closure-table-2.0.jar
```

访问 [http://localhost:6666](http://localhost:6666) 查看演示页面。

更多的用法及测试见 `CategoryStoreTest.java`
