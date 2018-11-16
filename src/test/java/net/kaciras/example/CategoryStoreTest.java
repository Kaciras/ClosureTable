package net.kaciras.example;

import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

final class CategoryStoreTest {

	/**
	 * 如果你想运行，下面这4个字段需要修改。
	 * 开发时使用的数据库是Mysql(Mariadb 10.2.9)，由于使用了
	 * 数据库方言，在其他数据库运行可能会失败。
	 */
	private static final String DB_DRIVER = "org.mariadb.jdbc.Driver";
	private static final String DB_URL = "jdbc:mariadb://localhost:3306/test";
	private static final String DB_USER = "用户名";
	private static final String DB_PASSWORD = "密码";

	private static Repository repository;
	private static SqlSession session;

	@BeforeAll
	static void init() throws Exception {
		UnpooledDataSource dataSource = MyConfig.createDataSource();
//		UnpooledDataSource dataSource = new UnpooledDataSource();
//		dataSource.setDriver(DB_DRIVER);
//		dataSource.setUrl(DB_URL);
//		dataSource.setUsername(DB_USER);
//		dataSource.setPassword(DB_PASSWORD);
		dataSource.setAutoCommit(false);

		TransactionFactory transactionFactory = new JdbcTransactionFactory();
		Environment environment = new Environment("test", transactionFactory, dataSource);
		
		Configuration config = new Configuration();
		config.addMapper(CategoryMapper.class);
		config.setEnvironment(environment);
		
		SqlSessionFactory sessionFactory = new DefaultSqlSessionFactory(config);
		session = sessionFactory.openSession();

		CategoryMapper mapper = session.getMapper(CategoryMapper.class);
		repository = new Repository(mapper);
		Category.categoryMapper = mapper;

		/*
		 * 测试数据如下:
		 *
		 *             0
		 *           /   \
		 *      ................
		 *        1         11
		 *        |        /  \
		 *        2       12   13
		 *      / | \
		 *     3  4  5
		 *          /  \
		 *         6    7
		 *       /    /  \
		 *      8    9    10
		 */
		Utils.executeScript(dataSource.getConnection(), "table.sql");
		Utils.executeScript(dataSource.getConnection(), "data.sql");
	}
	
	@AfterAll
	static void close() throws SQLException {
		Statement statement = session.getConnection().createStatement();
		statement.execute("DROP TABLE category");
		statement.execute("DROP TABLE category_tree");
		session.commit(true);
		session.close();
	}

	@AfterEach
	void afterEach() {
		session.rollback(true);
	}
	
	private static Category exceptData(int id) {
		Category category = new Category();
		category.setId(id);
		category.setName("Name_" + id);
		category.setDescription("Desc_" + id);
		category.setCover("Cover_" + id);
		return category;
	}

	@Test
	void testAddAndGet() {
		/* 分类为null时抛异常 */
		assertThatThrownBy(() -> repository.add(null, 0)).isInstanceOf(IllegalArgumentException.class);

		/* 分类中有属性为null时抛异常 */
		Category c0 = new Category();
		assertThatThrownBy(() -> repository.add(c0, 0)).isInstanceOf(IllegalArgumentException.class);

		c0.setName("Name");
		c0.setCover("Cover");
		c0.setDescription("Desc");

		/* parent指定的分类不存在时抛异常 */
		assertThatThrownBy(() -> repository.add(c0, 567)).isInstanceOf(IllegalArgumentException.class);

		/* 设置属性后正常添加，返回id */
		assertThat(c0.getId()).isEqualTo(0);
		repository.add(c0, 0);
		assertThat(c0.getId()).isNotEqualTo(0);

		/* get方法参数错误时抛异常 */
		assertThatThrownBy(() -> repository.findById(-123)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> repository.findById(0)).isInstanceOf(IllegalArgumentException.class);

		/* 指定分类不存在时抛异常 */
		assertThat(repository.findById(123)).isNull();

		/* get出来的对象与原对象属性相同 */
		Category got = repository.findById(c0.getId());
		assertThat(got).isEqualToComparingFieldByField(c0);
	}

	@Test
	void testGetParent() {
		assertThat(repository.findById(2).getParent()).isEqualToComparingFieldByField(exceptData(1));
		assertThat(repository.findById(4).getParent()).isEqualToComparingFieldByField(exceptData(2));
		assertThat(repository.findById(1).getParent()).isNull();
	}

	@Test
	void testGetAncestor() {
		/* 方法参数错误时抛异常 */
		assertThatThrownBy(() -> repository.findById(4).getAncestor(-5)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> repository.findById(4).getAncestor(0)).isInstanceOf(IllegalArgumentException.class);

		assertThat(repository.findById(4).getAncestor(2)).isEqualToComparingFieldByField(exceptData(1));
		assertThat(repository.findById(1).getAncestor(2)).isNull();
	}

	@Test
	void testGetPath() {
		/* 方法参数错误时抛异常 */
		assertThatThrownBy(() -> repository.findById(7).getPathTo(-5)).isInstanceOf(IllegalArgumentException.class);

		/* 测试Category.getPath() */
		assertThat(repository.findById(5).getPath())
				.usingFieldByFieldElementComparator()
				.containsExactly(exceptData(1), exceptData(2), exceptData(5));

		/* 测试Category.getPathTo(int) */
		assertThat(repository.findById(7).getPathTo(2))
				.usingFieldByFieldElementComparator()
				.containsExactly(exceptData(5), exceptData(7));

		/* 结果不存在时返回空列表 */
		assertThat(repository.findById(5).getPathTo(123456)).isEmpty();
	}

	@Test
	void testfindByAncestor() {
		/* 方法参数错误时抛异常 */
		assertThatThrownBy(() -> repository.findChildren(-123)).isInstanceOf(IllegalArgumentException.class);

		assertThat(repository.findChildren(666)).isEmpty();

		/* 测试getSubCategories(int)结果的正确性 */
		assertThat(repository.findChildren(exceptData(2).getId()))
				.usingFieldByFieldElementComparator().containsExactly(exceptData(3), exceptData(4), exceptData(5));

		/* 测试getSubCategories(int, int)结果的正确性 */
		assertThat(repository.findChildren(exceptData(2).getId(), 3))
				.usingFieldByFieldElementComparator().containsExactly(exceptData(8), exceptData(9), exceptData(10));
	}

	@Test
	void testCount() {
		assertThatThrownBy(() -> repository.countOfLayer(-1)).isInstanceOf(IllegalArgumentException.class);

		assertThat(repository.count()).isEqualTo(13);
		assertThat(repository.countOfLayer(5)).isEqualTo(3);
	}

	@Test
	void testGetLevel() {
		assertThat(repository.findById(9).getLevel()).isEqualTo(5);
		assertThat(repository.findById(11).getLevel()).isEqualTo(1);
	}

	@Test
	void testMove() {
		assertThatThrownBy(() -> repository.findById(2).moveTo(-5))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> repository.findById(2).moveTo(2))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> repository.findById(2).moveTo(123))
				.isInstanceOf(IllegalArgumentException.class);

		repository.findById(2).moveTo(7);

		assertThat(repository.findChildren(1))
				.usingFieldByFieldElementComparator()
				.containsExactlyInAnyOrder(exceptData(3), exceptData(4), exceptData(5));

		assertThat(repository.findById(2).getParent()).isEqualToComparingFieldByField(exceptData(7));
	}

	@Test
	void testMoveTree() {
		assertThatThrownBy(() -> repository.findById(2).moveTreeTo(-5))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> repository.findById(2).moveTreeTo(2))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> repository.findById(2).moveTreeTo(123))
				.isInstanceOf(IllegalArgumentException.class);


		repository.findById(2).moveTreeTo(7);

		/* 测试结果的正确性 */
		assertThat(repository.findChildren(exceptData(1).getId()))
				.usingFieldByFieldElementComparator()
				.containsExactlyInAnyOrder(exceptData(7));
		assertThat(repository.findChildren(exceptData(2).getId()))
				.usingFieldByFieldElementComparator()
				.containsExactlyInAnyOrder(exceptData(3), exceptData(4), exceptData(5));
		assertThat(repository.findById(2).getParent())
				.isEqualToComparingFieldByField(exceptData(7));
	}

	@Test
	void testDelete() {
		/* 方法参数错误时抛异常 */
		assertThatThrownBy(() -> repository.delete(-123)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> repository.delete(0)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> repository.delete(45)).isInstanceOf(IllegalArgumentException.class);

		repository.delete(exceptData(1).getId());

		/* 测试结果的正确性 */
		assertThat(repository.findChildren(0))
				.usingFieldByFieldElementComparator().containsExactlyInAnyOrder(exceptData(2), exceptData(11));
		assertThat(repository.findById(7).getPath())
				.usingFieldByFieldElementComparator()
				.containsExactly(exceptData(2), exceptData(5), exceptData(7));
	}

	@Test
	void testDeleteTree() {
		assertThatThrownBy(() -> repository.deleteTree(-123)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> repository.deleteTree(0)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> repository.deleteTree(45)).isInstanceOf(IllegalArgumentException.class);

		repository.deleteTree(5);

		assertThat(repository.findById(7)).isNull();
		assertThat(repository.findById(8)).isNull();

		assertThat(repository.findChildren(2))
				.usingFieldByFieldElementComparator()
				.containsExactlyInAnyOrder(exceptData(3), exceptData(4));
	}

	@Test
	void testUpdate() {
		Category categoryDTO = new Category();
		categoryDTO.setName("NewName");
		categoryDTO.setDescription("NewDesc");
		categoryDTO.setCover("NewCover");

		/* 不能更新顶级分类 */
		assertThatThrownBy(() -> repository.update(categoryDTO)).isInstanceOf(IllegalArgumentException.class);

		categoryDTO.setId(1);
		repository.update(categoryDTO);

		assertThat(repository.findById(exceptData(1).getId())).isEqualToComparingFieldByField(categoryDTO);
	}
}
