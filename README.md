# ClosureTable

基于ClosureTable的数据库无限级分类存储实现。

![ClosureTable示例](https://github.com/Kaciras/ClosureTableCateogryStore/blob/master/ClosureTable.png)

ClosureTable以一张表存储节点之间的关系、其中包含了任何两个有关系（祖先与子代）节点的关联信息。其包含3个字段：
                                                     
* `ancestor` 祖先：祖先节点的id
* `descendant` 子代：子代节点的id
* `distance` 距离：子代到祖先中间隔了几代

ClosureTable能够很好地解决树结构在关系数据库中的查询需求。

# 运行演示

运行需要设置MySQL或Mariadb数据库连接，需要替换下面第三行的命令中的参数。
```bash
mvn package
cd target
java -jar closure-table-1.0.jar jdbc:mariadb://localhost:3306/test root password
```

更多的用法及测试见 `CategoryStoreTest.java`

配套文章 [https://blog.kaciras.net/article/36/store-tree-in-database](https://blog.kaciras.net/article/36/store-tree-in-database)