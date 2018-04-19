# ClosureTable

基于ClosureTable的数据库无限级分类存储实现。

![ClosureTable示例](https://blog.kaciras.net/image/AE18CD3D37C00AEC5977B531F8559915EDCF1232E3AF2E3047A5D61CF3E15393.png)

ClosureTable以一张表存储节点之间的关系、其中包含了任何两个有关系（祖先与子代）节点的关联信息。其包含3个字段：
                                                     
* `ancestor` 祖先：祖先节点的id
* `descendant` 子代：子代节点的id
* `distance` 距离：子代到祖先中间隔了几代

ClosureTable能够很好地解决树结构的查询需求。

基本用法：

```java
/*
 * 创建Mybatis的SessionFactory,getMySqlSessionFactory方法需要你自己去实现
 * 或者参考CategoryStoreTest.init()
 */
SqlSessionFactory sessionFactory = getMySqlSessionFactory(); 
Utils.executeScript(sessionFactory.openSession().getConnection(), "table.sql"); //运行建表脚本

session = sessionFactory.openSession();
CategoryStore store = new ClosureTableCategoryStore(session.getMapper(CategoryMapper.class));

Category category = new Category();
category.setName("foo");
category.setDescription("bar");
category.setCover("cover.jpg");

int id = store.add(category, 0);
Category got = store.get(id);
Assertions.assertThat(got).isEqualToComparingFieldByField(got);
```

完整功能和请见 `CategoryStore.java`

更多的用法及测试见 `CategoryStoreTest.java`

配套文章 [https://blog.kaciras.net/article/36](https://blog.kaciras.net/article/36)