package kaciras;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// 目前只简单比较下 ID，SQL 里都用的 * 所以其它属性应该不会出问题。
final class CategoryAssert {

	public static void assertList(List<Category> list, int... expect) {
		var ids = list.stream().mapToInt(Category::getId).toArray();
		assertThat(ids).containsExactly(expect);
	}

	public static void assertContain(List<Category> list, int... expect) {
		var ids = list.stream().mapToInt(Category::getId).toArray();
		assertThat(ids).containsExactlyInAnyOrder(expect);
	}
}
