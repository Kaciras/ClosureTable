package kaciras;

import lombok.Getter;
import lombok.Setter;

/**
 * 比 Category 多了个 parentId 属性，只用于演示页面中的查询。
 */
@Getter
@Setter
final class ListQueryVO {
	private int id;
	private Integer parentId;
	private String name;
}
