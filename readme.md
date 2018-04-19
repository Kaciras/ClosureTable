# ClosureTable

基于ClosureTable的数据库无限级分类存储实现。

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