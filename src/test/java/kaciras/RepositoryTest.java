package kaciras;

import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

final class RepositoryTest {

	private static final String[] NAMES = {"root", "电子产品", "电脑配件", "硬盘", "CPU",
			"显卡", "AMD", "NVIDIA", "RX580", "GTX690战术核显卡", "RTX3080", "水果", "苹果", "西瓜"};

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

	/**
	 * 生成一个指定ID的分类对象，其属性与测试数据一致，作为断言时的预期对象。
	 *
	 * @param id 分类ID
	 * @return 分类对象
	 */
	private static Category testCategory(int id) {
		var category = new Category();
		category.setId(id);
		category.setName(NAMES[id]);
		return category;
	}

	@Test
	void addInvalid() {
		/* 分类名为 null 时抛异常 */
		var category = new Category();
		assertThatThrownBy(() -> repository.add(category)).isInstanceOf(IllegalArgumentException.class);

		/* parent 指定的分类不存在时抛异常 */
		category.setName("Name");
		category.setParentId(567);
		assertThatThrownBy(() -> repository.add(category)).isInstanceOf(IllegalArgumentException.class);
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
		repository.add(category);
		assertThat(category.getId()).isGreaterThan(0);

		/* findById 出来的对象与原对象属性相同 */
		var got = repository.findById(category.getId());
		assertThat(got).usingRecursiveComparison().isEqualTo(category);
	}

	@Test
	void count() {
		assertThat(repository.size()).isEqualTo(13);
	}

	@Test
	void delete() {
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
	void deleteTree() {
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
	void update() {
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
