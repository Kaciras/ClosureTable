package kaciras;

import lombok.RequiredArgsConstructor;
import org.apache.ibatis.exceptions.PersistenceException;

import java.util.List;

/**
 * 分类存储，提供对分类增删改查的支持。
 * <p>
 * 每个分类都可以拥有若干子分类，但最多只能属于一个父分类，没有父分类的称为根分类。
 * 分类之间的关系可以看做一棵多叉数。
 * <p>
 * 分类树有一个根节点，其 ID 为 0，不可移动或删除。
 *
 * @author Kaciras
 */
@RequiredArgsConstructor
public class Repository {

	private final CategoryMapper mapper;

	/**
	 * 根据指定的 ID，查询出分类对象。
	 *
	 * @param id 分类的 ID
	 * @return 分类对象，如果不存在则为 null
	 */
	public Category findById(int id) {
		Utils.checkNotNegative(id, "id");
		return mapper.selectById(id);
	}

	/**
	 * 获取所有分类的数量，不包括根分类。
	 *
	 * @return 分类数量
	 */
	public int size() {
		return mapper.selectCount() - 1;
	}

	/**
	 * 新增一个分类，自动生成并设置 ID 属性。
	 *
	 * @param category 分类实体对象
	 */
	public void add(Category category, Category parent) {
		try {
			mapper.insert(category);
			mapper.insertPath(category.getId(), parent.getId());
			mapper.insertSelfLink(category.getId());
		} catch (PersistenceException ex) {
			throw new IllegalArgumentException(ex);
		}
	}

	/**
	 * 该方法仅更新分类的属性，不修改继承关系，若要移动节点请使用
	 * <code>Category.moveTo()</code> 和 <code>Category.moveTreeTo()</code>
	 *
	 * @param category 包含新属性的分类对象
	 */
	public void update(Category category) {
		Utils.checkEffective(mapper.update(category));
	}

	/**
	 * 删除一个分类，原来在该分类下的子分类将被移动到该分类的父分类中，
	 * 如果此分类是一级分类，则删除后子分类将全部成为一级分类。根分类不可删除。
	 *
	 * @param id 要删除的分类的 ID
	 * @throws IllegalArgumentException 如果指定 ID 的分类不存在
	 */
	public void delete(int id) {
		Utils.checkPositive(id, "id");

		var category = findById(id);
		if (category == null) {
			throw new IllegalArgumentException("指定的分类不存在");
		}

		// 已排除根节点，故 parent 不会为 null。
		var parent = category.getAncestorId(1);
		deleteFromAllTable(id);
		category.moveSubTree(parent);
	}

	/**
	 * 删除一个分类及其所有的下级分类，顶级分类不可删除。
	 *
	 * @param id 要删除的分类的 ID
	 * @throws IllegalArgumentException 如果指定 ID 的分类不存在
	 */
	public void deleteTree(int id) {
		Utils.checkPositive(id, "id");

		var category = findById(id);
		if (category == null) {
			throw new IllegalArgumentException("指定的分类不存在");
		}
		deleteFromAllTable(id);
		mapper.selectDescendantId(id).forEach(this::deleteFromAllTable);
	}

	/**
	 * 删除两个表中与分类相关的记录。
	 *
	 * @param id 分类的 ID
	 */
	private void deleteFromAllTable(int id) {
		mapper.delete(id);
		mapper.deletePath(id);
	}

	/**
	 * 专用于演示页面的查询方法，查询结果中多了个 parentId 字段。
	 *
	 * @return 带父 ID 的分类列表
	 */
	public List<ListQueryVO> getAllForDemo() {
		return mapper.selectAllWithParent();
	}
}
