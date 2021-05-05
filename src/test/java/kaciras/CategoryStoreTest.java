package kaciras;

import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

final class CategoryStoreTest {

	private static Repository repository;
	private static SqlSession session;

	@BeforeAll
	static void init() throws Exception {
		session = Utils.createSqlSession();

		var mapper = session.getMapper(CategoryMapper.class);
		repository = new Repository(mapper);

		// 如果使用Spring，可以用@Configurable来注入此依赖。
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
		Utils.executeScript(session.getConnection(), "schema.sql");
		Utils.executeScript(session.getConnection(), "data.sql");
	}

	@AfterAll
	static void close() {
		Utils.dropTables(session.getConnection());
		session.close();
	}

	@AfterEach
	void afterEach() {
		session.rollback(true);
	}

	/**
	 * 生成一个指定ID的分类对象，其属性与测试数据一致，作为断言时的预期对象。
	 *
	 * @param id 分类ID
	 * @return 分类对象
	 */
	private static Category testCategory(int id) {
		var category = new Category();
		category.setId(id);
		category.setName("Name_" + id);
		return category;
	}

	@Test
	void testAddAndGet() {
		/* 分类为null时抛异常 */
		assertThatThrownBy(() -> repository.add(null, 0))
				.isInstanceOf(IllegalArgumentException.class);

		/* 分类中有属性为null时抛异常 */
		var c0 = new Category();
		assertThatThrownBy(() -> repository.add(c0, 0))
				.isInstanceOf(IllegalArgumentException.class);

		c0.setName("Name");

		/* parent指定的分类不存在时抛异常 */
		assertThatThrownBy(() -> repository.add(c0, 567))
				.isInstanceOf(IllegalArgumentException.class);

		/* 设置属性后正常添加，并设置对象的id */
		assertThat(c0.getId()).isEqualTo(0);
		repository.add(c0, 0);
		assertThat(c0.getId()).isNotEqualTo(0);

		/* get方法参数错误时抛异常 */
		assertThatThrownBy(() -> repository.findById(-123))
				.isInstanceOf(IllegalArgumentException.class);

		/* 指定分类不存在返回null */
		assertThat(repository.findById(123)).isNull();

		/* get出来的对象与原对象属性相同 */
		var got = repository.findById(c0.getId());
		assertThat(got).usingRecursiveComparison().isEqualTo(c0);
	}

	@Test
	void testGetParent() {
		assertThat(repository.findById(2).getParent()).usingRecursiveComparison().isEqualTo(testCategory(1));
		assertThat(repository.findById(4).getParent()).usingRecursiveComparison().isEqualTo(testCategory(2));
		assertThat(repository.findById(0).getParent()).isNull();
	}

	@Test
	void testGetAncestor() {
		/* 方法参数错误时抛异常 */
		assertThatThrownBy(() -> repository.findById(4).getAncestor(-5))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> repository.findById(4).getAncestor(0))
				.isInstanceOf(IllegalArgumentException.class);

		assertThat(repository.findById(4).getAncestor(2))
				.usingRecursiveComparison().isEqualTo(testCategory(1));

		assertThat(repository.findById(1).getAncestor(2)).isNull();
	}

	@Test
	void testGetPath() {
		/* 方法参数错误时抛异常 */
		assertThatThrownBy(() -> repository.findById(7).getPathRelativeTo(-5))
				.isInstanceOf(IllegalArgumentException.class);

		/* 测试 Category.getPath() */
		assertThat(repository.findById(5).getPath())
				.usingFieldByFieldElementComparator()
				.containsExactly(testCategory(1), testCategory(2), testCategory(5));

		/* 测试Category.getPathRelativeTo(int) */
		assertThat(repository.findById(7).getPathRelativeTo(2))
				.usingFieldByFieldElementComparator()
				.containsExactly(testCategory(5), testCategory(7));

		/* 结果不存在时返回空列表 */
		assertThat(repository.findById(5).getPathRelativeTo(12)).isEmpty();
	}

	@Test
	void testGetChildren() {
		/* 测试 getChildren() 结果的正确性 */
		assertThat(repository.findById(2).getChildren())
				.usingFieldByFieldElementComparator()
				.containsExactly(testCategory(3), testCategory(4), testCategory(5));

		/* 测试 getChildren(int) 结果的正确性 */
		assertThat(repository.findById(2).getChildren(3))
				.usingFieldByFieldElementComparator()
				.containsExactly(testCategory(8), testCategory(9), testCategory(10));
	}

	@Test
	void testCount() {
		assertThatThrownBy(() -> repository.countOfLayer(-1))
				.isInstanceOf(IllegalArgumentException.class);

		/* 树中有13个分类，不包括ID为0的根分类 */
		assertThat(repository.count()).isEqualTo(13);
		assertThat(repository.countOfLayer(5)).isEqualTo(3);
	}

	@Test
	void testGetLevel() {
		assertThat(repository.findById(9).getLevel()).isEqualTo(5);
		assertThat(repository.findById(11).getLevel()).isEqualTo(1);
	}

	/**
	 *       1                                    1
	 *       |                                  / | \
	 *       2                                 3  4  5
	 *     / | \         (id=2).moveTo(7)           / \
	 *    3  4  5       ----------------->         6   7
	 *         / \                                /  / | \
	 *       6    7                              8  9  10 2
	 *      /    /  \
	 *     8    9    10
	 */
	@Test
	void testMove() {
		assertThatThrownBy(() -> repository.findById(2).moveTo(-5))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> repository.findById(2).moveTo(123))
				.isInstanceOf(IllegalArgumentException.class);

		/* 不能移动到自己下面，根分类也不能够移动 */
		assertThatThrownBy(() -> repository.findById(2).moveTo(2))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> repository.findById(0).moveTo(2))
				.isInstanceOf(UnsupportedOperationException.class);

		repository.findById(2).moveTo(7);

		/* 移动后具有新的父节点 */
		assertThat(repository.findById(2).getParent())
				.usingRecursiveComparison().isEqualTo(testCategory(7));

		/* 子树自动升级 */
		assertThat(repository.findById(1).getChildren())
				.usingFieldByFieldElementComparator()
				.containsExactlyInAnyOrder(testCategory(3), testCategory(4), testCategory(5));
	}

	/**
	 *       1                                      1
	 *       |                                      |
	 *       2                                      7
	 *     / | \       (id=2).moveTreeTo(7)       / | \
	 *    3  4  5      -------------------->     9  10  2
	 *         / \                                  / | \
	 *       6    7                                3  4  5
	 *      /    /  \                                    |
	 *     8    9    10                                  6
	 *                                                   |
	 *                                                   8
	 */
	@Test
	void testMoveTree() {
		assertThatThrownBy(() -> repository.findById(2).moveTreeTo(-5))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> repository.findById(2).moveTreeTo(123))
				.isInstanceOf(IllegalArgumentException.class);

		/* 不能移动到自己下面，根分类也不能够移动 */
		assertThatThrownBy(() -> repository.findById(2).moveTreeTo(2))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> repository.findById(0).moveTreeTo(2))
				.isInstanceOf(UnsupportedOperationException.class);

		repository.findById(2).moveTreeTo(7);

		/* 移动后具有新的父节点 */
		assertThat(repository.findById(2).getParent())
				.usingRecursiveComparison().isEqualTo(testCategory(7));

		/* 子树也随之移动 */
		assertThat(repository.findById(1).getChildren())
				.usingFieldByFieldElementComparator()
				.containsExactlyInAnyOrder(testCategory(7));
		assertThat(repository.findById(2).getChildren())
				.usingFieldByFieldElementComparator()
				.containsExactlyInAnyOrder(testCategory(3), testCategory(4), testCategory(5));
	}

	@Test
	void testDelete() {
		/* 方法参数错误时抛异常 */
		assertThatThrownBy(() -> repository.delete(-123)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> repository.delete(0)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> repository.delete(45)).isInstanceOf(IllegalArgumentException.class);

		repository.delete(testCategory(1).getId());

		assertThat(repository.findById(0).getChildren())
				.usingFieldByFieldElementComparator()
				.containsExactlyInAnyOrder(testCategory(2), testCategory(11));
		assertThat(repository.findById(7).getPath())
				.usingFieldByFieldElementComparator()
				.containsExactly(testCategory(2), testCategory(5), testCategory(7));
	}

	@Test
	void testDeleteTree() {
		assertThatThrownBy(() -> repository.deleteTree(-123)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> repository.deleteTree(0)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> repository.deleteTree(45)).isInstanceOf(IllegalArgumentException.class);

		repository.deleteTree(5);

		assertThat(repository.findById(7)).isNull();
		assertThat(repository.findById(8)).isNull();

		assertThat(repository.findById(2).getChildren())
				.usingFieldByFieldElementComparator()
				.containsExactlyInAnyOrder(testCategory(3), testCategory(4));
	}

	@Test
	void testUpdate() {
		var categoryDTO = new Category();
		categoryDTO.setId(999);
		categoryDTO.setName("NewName");

		/* 不能更新不存在的分类 */
		assertThatThrownBy(() -> repository.update(categoryDTO))
				.isInstanceOf(IllegalArgumentException.class);

		categoryDTO.setId(1);
		repository.update(categoryDTO);

		assertThat(repository.findById(testCategory(1).getId()))
				.usingRecursiveComparison().isEqualTo(categoryDTO);
	}
}
