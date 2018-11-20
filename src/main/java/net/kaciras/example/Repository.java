package net.kaciras.example;

import lombok.RequiredArgsConstructor;
import org.apache.ibatis.exceptions.PersistenceException;

/**
 * 分类存储，提供对分类的增删改查等操作的支持。
 * <p>
 * 类别（分类）是用于归类、整理文章资源的数据信息。
 * 每个分类都可以拥有若干子分类，但最多只能属于一个父分类，没有父分类的称为
 * 顶级分类。分类的从属关系可以看做一棵多叉数。
 * <p>
 * 除了相互之间的关系外，分类拥有ID、名称这两个属性。其中ID为int，
 * 由数据库自动生成，你也可以添加其它属性。
 * <p>
 * 分类树有一个根节点，其ID为0，且不可修改ID、移动和删除。
 *
 * @author Kaciras
 */
@RequiredArgsConstructor
public class Repository {

	private final CategoryMapper categoryMapper;

	/**
	 * 根据指定的id，获取分类的全部属性。
	 *
	 * @param id 分类id
	 * @return 分类的实体对象
	 * @throws IllegalArgumentException 如果id不是正数
	 */
	public Category findById(int id) {
		Utils.checkNotNegative(id, "id");
		return categoryMapper.selectAttributes(id);
	}

	/**
	 * 获取所有分类的数量。
	 *
	 * @return 数量
	 */
	public int count() {
		return categoryMapper.selectCount() - 1;
	}

	/**
	 * 获取某一级分类的数量，参数从1开始，表示第一级分类（根分类的子类）。
	 *
	 * @param layer 层级（从1开始）
	 * @return 数量
	 * @throws IllegalArgumentException 如果layer不是正数
	 */
	public int countOfLayer(int layer) {
		Utils.checkPositive(layer, "layer");
		return categoryMapper.selectCountByLayer(layer);
	}

	/**
	 * 新增一个分类，其ID属性将自动生成或计算，并返回。
	 * 新增分类的继承关系由parent属性指定，parent为0表示该分类为一级分类。
	 *
	 * @param category 分类实体对象
	 * @param parent   上级分类id
	 * @throws IllegalArgumentException 如果parent所指定的分类不存在、category为null或category中存在属性为null
	 */
	public int add(Category category, int parent) {
		Utils.checkNotNegative(parent, "parent");
		if (parent > 0 && categoryMapper.contains(parent) == null) {
			throw new IllegalArgumentException("指定的上级分类不存在");
		}
		try {
			categoryMapper.insert(category);
			categoryMapper.insertPath(category.getId(), parent);
			categoryMapper.insertNode(category.getId());
		} catch (PersistenceException ex) {
			throw new IllegalArgumentException(ex);
		}
		return category.getId();
	}

	/**
	 * 该方法仅更新分类的属性，不修改继承关系，若要移动节点请使用
	 * <code>Category.moveTo()</code>和<code>Category.moveTreeTo()</code>
	 *
	 * @param category 新的分类信息对象
	 */
	public void update(Category category) {
		try {
			Utils.checkEffective(categoryMapper.update(category));
		} catch (PersistenceException ex) {
			throw new IllegalArgumentException(ex);
		}
	}

	/**
	 * 删除一个分类，原来在该分类下的子分类将被移动到该分类的父分类中，
	 * 如果此分类是一级分类，则删除后子分类将全部成为一级分类。
	 * <p>
	 * 顶级分类不可删除。
	 *
	 * @param id 要删除的分类的id
	 * @throws IllegalArgumentException 如果指定id的分类不存在
	 */
	public void delete(int id) {
		Utils.checkPositive(id, "id");

		if (categoryMapper.contains(id) == null) {
			throw new IllegalArgumentException("指定的分类不存在");
		}
		Integer parent = categoryMapper.selectAncestor(id, 1);
		if (parent == null) {
			parent = 0;
		}
		findById(id).moveSubTree(parent);
		deleteBoth(id);
	}

	/**
	 * 删除一个分类及其所有的下级分类。
	 * <p>
	 * 顶级分类不可删除。
	 *
	 * @param id 要删除的分类的id
	 * @throws IllegalArgumentException 如果指定id的分类不存在
	 */
	public void deleteTree(int id) {
		Utils.checkPositive(id, "id");

		if (categoryMapper.contains(id) == null) {
			throw new IllegalArgumentException("指定的分类不存在");
		}
		deleteBoth(id);
		for (int des : categoryMapper.selectDescendantId(id)) {
			deleteBoth(des);
		}
	}

	/**
	 * 删除一个分类，两个表中的相关记录都删除
	 *
	 * @param id 分类id
	 */
	private void deleteBoth(int id) {
		Utils.checkEffective(categoryMapper.delete(id));
		categoryMapper.deletePath(id);
	}
}
