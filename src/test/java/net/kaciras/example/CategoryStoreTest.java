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

	private static CategoryStore categoryStore;
	private static SqlSession session;

	@BeforeAll
	static void init() throws Exception {
		UnpooledDataSource dataSource = new UnpooledDataSource();
		dataSource.setDriver(DB_DRIVER);
		dataSource.setUrl(DB_URL);
		dataSource.setUsername(DB_USER);
		dataSource.setPassword(DB_PASSWORD);
		dataSource.setAutoCommit(false);

		TransactionFactory transactionFactory = new JdbcTransactionFactory();
		Environment environment = new Environment("test", transactionFactory, dataSource);
		
		Configuration config = new Configuration();
		config.addMapper(CategoryMapper.class);
		config.setEnvironment(environment);
		
		SqlSessionFactory sessionFactory = new DefaultSqlSessionFactory(config);
		session = sessionFactory.openSession();
		categoryStore = new ClosureTableCategoryStore(session.getMapper(CategoryMapper.class));

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
		statement.execute("DROP TABLE Category");
		statement.execute("DROP TABLE CategoryTree");
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
		assertThatThrownBy(() -> categoryStore.add(null, 0)).isInstanceOf(IllegalArgumentException.class);

		/* 分类中有属性为null时抛异常 */
		Category c0 = new Category();
		assertThatThrownBy(() -> categoryStore.add(c0, 0)).isInstanceOf(IllegalArgumentException.class);

		c0.setName("Name");
		c0.setCover("Cover");
		c0.setDescription("Desc");

		/* parent指定的分类不存在时抛异常 */
		assertThatThrownBy(() -> categoryStore.add(c0, 567)).isInstanceOf(IllegalArgumentException.class);

		/* 设置属性后正常添加，返回id */
		assertThat(c0.getId()).isEqualTo(0);
		categoryStore.add(c0, 0);
		assertThat(c0.getId()).isNotEqualTo(0);

		/* get方法参数错误时抛异常 */
		assertThatThrownBy(() -> categoryStore.get(-123)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> categoryStore.get(0)).isInstanceOf(IllegalArgumentException.class);

		/* 指定分类不存在时抛异常 */
		assertThat(categoryStore.get(123)).isNull();

		/* get出来的对象与原对象属性相同 */
		Category got = categoryStore.get(c0.getId());
		assertThat(got).isEqualToComparingFieldByField(c0);
	}

	@Test
	void testGetParent() {
		/* 方法参数错误时抛异常 */
		assertThatThrownBy(() -> categoryStore.getParent(-123)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> categoryStore.getParent(0)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> categoryStore.getParent(1)).isInstanceOf(IllegalArgumentException.class);

		assertThat(categoryStore.getParent(exceptData(2).getId())).isEqualToComparingFieldByField(exceptData(1));
		assertThat(categoryStore.getParent(exceptData(4).getId())).isEqualToComparingFieldByField(exceptData(2));
		assertThatThrownBy(() -> categoryStore.getParent(exceptData(1).getId())).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void testGetAncestor() {
		/* 方法参数错误时抛异常 */
		assertThatThrownBy(() -> categoryStore.getAncestor(1, -123)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> categoryStore.getAncestor(1, 0)).isInstanceOf(IllegalArgumentException.class);

		assertThat(categoryStore.getAncestor(exceptData(4).getId(), 2)).isEqualToComparingFieldByField(exceptData(1));
		assertThat(categoryStore.getAncestor(exceptData(1).getId(), 2)).isNull();
	}

	@Test
	void testGetPath() {
		/* 方法参数错误时抛异常 */
		assertThatThrownBy(() -> categoryStore.getPath(-123)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> categoryStore.getPath(0)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> categoryStore.getPath(-123, 1)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> categoryStore.getPath(0, 1)).isInstanceOf(IllegalArgumentException.class);

		/* 测试getPath(int)结果的正确性 */
		assertThat(categoryStore.getPath(exceptData(5).getId()))
				.usingFieldByFieldElementComparator().containsExactly(exceptData(1), exceptData(2), exceptData(5));

		/* 测试getPath(int, int)结果的正确性 */
		assertThat(categoryStore.getPath(exceptData(7).getId(), exceptData(2).getId()))
				.usingFieldByFieldElementComparator()
				.containsExactly(exceptData(5), exceptData(7));

		/* 结果不存在时返回空列表 */
		assertThat(categoryStore.getPath(12345)).isEmpty();
		assertThat(categoryStore.getPath(exceptData(6).getId(), 12345)).isEmpty();
		assertThat(categoryStore.getPath(exceptData(6).getId(), exceptData(6).getId())).isEmpty();
	}

	@Test
	void testGetSubCategories() {
		/* 方法参数错误时抛异常 */
		assertThatThrownBy(() -> categoryStore.getSubCategories(-123)).isInstanceOf(IllegalArgumentException.class);

		assertThat(categoryStore.getSubCategories(666)).isEmpty();

		/* 测试getSubCategories(int)结果的正确性 */
		assertThat(categoryStore.getSubCategories(exceptData(2).getId()))
				.usingFieldByFieldElementComparator().containsExactly(exceptData(3), exceptData(4), exceptData(5));

		/* 测试getSubCategories(int, int)结果的正确性 */
		assertThat(categoryStore.getSubCategories(exceptData(2).getId(), 3))
				.usingFieldByFieldElementComparator().containsExactly(exceptData(8), exceptData(9), exceptData(10));
	}

	@Test
	void testGetCount() {
		assertThatThrownBy(() -> categoryStore.getCount(-1)).isInstanceOf(IllegalArgumentException.class);

		assertThat(categoryStore.getCount()).isEqualTo(13);
		assertThat(categoryStore.getCount(5)).isEqualTo(3);
	}

	@Test
	void testGetLevel() {
		assertThatThrownBy(() -> categoryStore.getLevel(-123)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> categoryStore.getLevel(0)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> categoryStore.getLevel(123)).isInstanceOf(IllegalArgumentException.class);

		assertThat(categoryStore.getLevel(exceptData(9).getId())).isEqualTo(5);
		assertThat(categoryStore.getLevel(exceptData(11).getId())).isEqualTo(1);
	}

	@Test
	void testMove() {
		assertThatThrownBy(() -> categoryStore.move(-123, 45)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> categoryStore.move(123, -45)).isInstanceOf(IllegalArgumentException.class);

		assertThatThrownBy(() -> categoryStore.move(exceptData(2).getId(), exceptData(2).getId()))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> categoryStore.move(123, exceptData(7).getId()))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> categoryStore.move(exceptData(2).getId(), 45))
				.isInstanceOf(IllegalArgumentException.class);

		categoryStore.move(exceptData(2).getId(), exceptData(7).getId());

		assertThat(categoryStore.getSubCategories(1))
				.usingFieldByFieldElementComparator()
				.containsExactlyInAnyOrder(exceptData(3), exceptData(4), exceptData(5));
		assertThat(categoryStore.getParent(2)).isEqualToComparingFieldByField(exceptData(7));
	}

	@Test
	void testMoveTree() {
		/* 方法参数错误时抛异常 */
		assertThatThrownBy(() -> categoryStore.moveTree(-123, 45)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> categoryStore.moveTree(123, -45)).isInstanceOf(IllegalArgumentException.class);

		/* 方法参数错误时抛异常2 */
		assertThatThrownBy(() -> categoryStore.moveTree(exceptData(2).getId(), exceptData(2).getId()))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> categoryStore.moveTree(123, exceptData(7).getId()))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> categoryStore.moveTree(exceptData(2).getId(), 45))
				.isInstanceOf(IllegalArgumentException.class);

		categoryStore.moveTree(exceptData(2).getId(), exceptData(7).getId());

		/* 测试结果的正确性 */
		assertThat(categoryStore.getSubCategories(exceptData(1).getId()))
				.usingFieldByFieldElementComparator().containsExactlyInAnyOrder(exceptData(7));
		assertThat(categoryStore.getSubCategories(exceptData(2).getId()))
				.usingFieldByFieldElementComparator().containsExactlyInAnyOrder(exceptData(3), exceptData(4), exceptData(5));
		assertThat(categoryStore.getParent(2)).isEqualToComparingFieldByField(exceptData(7));
	}

	@Test
	void testDelete() {
		/* 方法参数错误时抛异常 */
		assertThatThrownBy(() -> categoryStore.delete(-123)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> categoryStore.delete(0)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> categoryStore.delete(45)).isInstanceOf(IllegalArgumentException.class);

		categoryStore.delete(exceptData(1).getId());

		/* 测试结果的正确性 */
		assertThat(categoryStore.getSubCategories(0))
				.usingFieldByFieldElementComparator().containsExactlyInAnyOrder(exceptData(2), exceptData(11));
		assertThat(categoryStore.getPath(exceptData(7).getId()))
				.usingFieldByFieldElementComparator().containsExactly(exceptData(2), exceptData(5), exceptData(7));
	}

	@Test
	void testDeleteTree() {
		assertThatThrownBy(() -> categoryStore.deleteTree(-123)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> categoryStore.deleteTree(0)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> categoryStore.deleteTree(45)).isInstanceOf(IllegalArgumentException.class);

		categoryStore.deleteTree(5);

		assertThat(categoryStore.get(7)).isNull();
		assertThat(categoryStore.get(8)).isNull();

		assertThat(categoryStore.getSubCategories(2))
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
		assertThatThrownBy(() -> categoryStore.update(categoryDTO)).isInstanceOf(IllegalArgumentException.class);

		categoryDTO.setId(1);
		categoryStore.update(categoryDTO);

		assertThat(categoryStore.get(exceptData(1).getId())).isEqualToComparingFieldByField(categoryDTO);
	}
}
