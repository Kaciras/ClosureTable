package kaciras;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

final class CategoryAssert {

	public static void assertList(List<Category> list, int... expect) {
		var ids = list.stream().mapToInt(Category::getId).toArray();
		assertThat(ids).containsExactly(expect);
	}
}
