package kaciras;

import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

final class CategoryTest {

	private static Repository repository;
	private static SqlSession session;

	@BeforeAll
	static void init() throws Exception {
		session = Utils.createSqlSession(Utils.getDaraSource());

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

		var mapper = session.getMapper(CategoryMapper.class);

		// 如果使用 Spring，可以用 @Configurable 来注入此依赖。
		Category.mapper = mapper;
		repository = new Repository(mapper);
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

	@Test
	void getParent() {
		assertThat(repository.findById(2).getParentId()).isEqualTo(1);
		assertThat(repository.findById(4).getParentId()).isEqualTo(2);
		assertThat(repository.findById(0).getParentId()).isNull();
	}

	@Test
	void getAncestorId() {
		/* 方法参数错误时抛异常 */
		assertThatThrownBy(() -> repository.findById(4).getAncestorId(-5))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> repository.findById(4).getAncestorId(0))
				.isInstanceOf(IllegalArgumentException.class);

		assertThat(repository.findById(4).getAncestorId(2)).isEqualTo(1);
		assertThat(repository.findById(1).getAncestorId(2)).isNull();
	}

	@Test
	void getLevel() {
		assertThat(repository.findById(9).getLevel()).isEqualTo(5);
		assertThat(repository.findById(11).getLevel()).isEqualTo(1);
	}

	@Test
	void getPath() {
		var descendant = repository.findById(7);
		CategoryAssert.assertList(descendant.getPath(), 1, 2, 5, 7);
	}

	@Test
	void getPathToAncestor() {
		var descendant = repository.findById(7);
		var ancestor = repository.findById(2);
		CategoryAssert.assertList(descendant.getPath(ancestor), 5, 7);
	}

	@Test
	void getPathBetweenUnrelated() {
		var descendant = repository.findById(5);
		var ancestor = repository.findById(12);
		assertThat(descendant.getPath(ancestor)).isEmpty();
	}

	@Test
	void getChildren() {
		var children = repository.findById(2).getChildren();
		CategoryAssert.assertList(children, 3, 4, 5);
	}

	@Test
	void getSubLayer() {
		var list = repository.findById(2).getSubLayer(3);
		CategoryAssert.assertList(list, 8, 9, 10);
	}

	/* 不能移动到自己下面，根分类也不能够移动 */
	@Test
	void invalidMove() {
		var newParent = repository.findById(2);

		assertThatThrownBy(() -> repository.findById(2).moveTo(newParent))
				.isInstanceOf(IllegalArgumentException.class);

		assertThatThrownBy(() -> repository.findById(0).moveTo(newParent))
				.isInstanceOf(UnsupportedOperationException.class);
	}

	/*
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
	void move() {
		var newParent = repository.findById(7);

		repository.findById(2).moveTo(newParent);

		// 移动后具有新的父节点
		assertThat(repository.findById(2).getParentId()).isEqualTo(7);

		// 子树自动升级
		CategoryAssert.assertList(repository.findById(2).getChildren(), 3, 4, 5);
	}

	/* 不能移动到自己下面，根分类也不能够移动 */
	@Test
	void invalidMoveTree() {
		var newParent = repository.findById(2);

		assertThatThrownBy(() -> repository.findById(2).moveTreeTo(newParent))
				.isInstanceOf(IllegalArgumentException.class);

		assertThatThrownBy(() -> repository.findById(0).moveTreeTo(newParent))
				.isInstanceOf(UnsupportedOperationException.class);
	}

	/*
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
	void moveTree() {
		var newParent = repository.findById(7);

		repository.findById(2).moveTreeTo(newParent);

		/* 移动后具有新的父节点 */
		assertThat(repository.findById(2).getParentId()).isEqualTo(7);

		/* 子树也随之移动 */
		CategoryAssert.assertList(repository.findById(1).getChildren(), 7);
		CategoryAssert.assertList(repository.findById(2).getChildren(), 3, 4, 5);
	}
}
