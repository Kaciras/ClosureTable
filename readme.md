# ClosureTable

基于ClosureTable的数据库无限级分类存储实现。

![ClosureTable示例](https://github.com/Kaciras/ClosureTableCateogryStore/blob/master/ClosureTable.png)

ClosureTable以一张表存储节点之间的关系、其中包含了任何两个有关系（祖先与子代）节点的关联信息。其包含3个字段：
                                                     
* `ancestor` 祖先：祖先节点的id
* `descendant` 子代：子代节点的id
* `distance` 距离：子代到祖先中间隔了几代

ClosureTable能够很好地解决树结构的查询需求。

# 运行此项目

```java
/*
 * 创建Mybatis的SessionFactory,getMySqlSessionFactory方法需要你自己去实现
 * 或者参考CategoryStoreTest.init()
 */
SqlSessionFactory sessionFactory = getMySqlSessionFactory();
Utils.executeScript(sessionFactory.openSession().getConnection(), "table.sql"); //运行建表脚本

session = sessionFactory.openSession();
CategoryMapper mapper = session.getMapper(CategoryMapper.class);
Category.categoryMapper = mapper;
Repository repository = new Repository(mapper);

Category category = new Category();
category.setName("foo");
category.setDescription("bar");
category.setCover("cover.jpg");

int id = repository.add(category, 0);
Category got = repository.get(id);
Assertions.assertThat(got).isEqualToComparingFieldByField(got);
```

更多的用法及测试见 `CategoryStoreTest.java`

配套文章 [https://blog.kaciras.net/article/36](https://blog.kaciras.net/article/36)