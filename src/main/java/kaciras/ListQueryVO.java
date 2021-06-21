package kaciras;

import lombok.Getter;
import lombok.Setter;

/**
 * 比 Category 多了个 parentId 属性，只用于演示页面中的查询。
 * 因为要构建树状图必须知道节点间的关系。
 */
@Getter
@Setter
final class ListQueryVO {
	private int id;
	private Integer parentId;
	private String name;
}
