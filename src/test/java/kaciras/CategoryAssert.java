package kaciras;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

final class CategoryAssert {

	private static final String[] NAMES = {"root", "电子产品", "电脑配件", "硬盘", "CPU",
			"显卡", "AMD", "NVIDIA", "RX580", "GTX690战术核显卡", "RTX3080", "水果", "苹果", "西瓜"};

	public static void assertList(List<Category> list, int... expect) {
		var ids = list.stream().mapToInt(Category::getId).toArray();
		assertThat(ids).containsExactly(expect);
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
}
