package kaciras;

import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

final class RepositoryTest {

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
	void addInvalid() {
		/* 分类名为 null 时抛异常 */
		var category = new Category();
		assertThatThrownBy(() -> repository.add(category, 1)).isInstanceOf(IllegalArgumentException.class);

		/* parent 指定的分类不存在时抛异常 */
		category.setName("Name");
		assertThatThrownBy(() -> repository.add(category, 567)).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void findByInvalidId() {
		assertThatThrownBy(() -> repository.findById(-123)).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void getNonExists() {
		assertThat(repository.findById(123)).isNull();
	}

	@Test
	void addAndGet() {
		var category = new Category();
		category.setName("Name");

		/* 设置属性后正常添加，并设置对象的id */
		repository.add(category, 1);
		assertThat(category.getId()).isGreaterThan(0);

		/* findById 出来的对象与原对象属性相同 */
		var got = repository.findById(category.getId());
		assertThat(got).usingRecursiveComparison().isEqualTo(category);
	}

	@Test
	void count() {
		assertThat(repository.size()).isEqualTo(13);
	}

	/* 方法参数错误时抛异常 */
	@Test
	void invalidDelete() {
		assertThatThrownBy(() -> repository.delete(-123)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> repository.delete(0)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> repository.delete(45)).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void delete() {
		repository.delete(1);

		CategoryAssert.assertList(repository.findById(0).getChildren(), 2, 11);
		CategoryAssert.assertList(repository.findById(7).getPath(), 2, 5, 7);
	}

	@Test
	void invalidDeleteTree() {
		assertThatThrownBy(() -> repository.deleteTree(-123)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> repository.deleteTree(0)).isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> repository.deleteTree(45)).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void deleteTree() {
		repository.deleteTree(5);

		assertThat(repository.findById(7)).isNull();
		assertThat(repository.findById(8)).isNull();

		CategoryAssert.assertList(repository.findById(2).getChildren(), 3, 4);
	}

	@Test
	void updateNonExists() {
		var categoryDTO = new Category();
		categoryDTO.setId(999);
		categoryDTO.setName("NewName");

		assertThatThrownBy(() -> repository.update(categoryDTO))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void update() {
		var categoryDTO = new Category();
		categoryDTO.setId(1);
		categoryDTO.setName("NewName");

		repository.update(categoryDTO);

		assertThat(repository.findById(1)).usingRecursiveComparison().isEqualTo(categoryDTO);
	}
}
