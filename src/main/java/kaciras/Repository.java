package kaciras;

import lombok.RequiredArgsConstructor;
import org.apache.ibatis.exceptions.PersistenceException;

import java.util.List;

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
 * 分类树有一个根节点，其 ID 为 0，且不可修改 ID、移动和删除。
 *
 * @author Kaciras
 */
@RequiredArgsConstructor
public class Repository {

	private final CategoryMapper categoryMapper;

	/**
	 * 根据指定的 ID，查询出分类对象。
	 *
	 * @param id 分类 ID
	 * @return 分类对象，如果不存在则为 null
	 */
	public Category findById(int id) {
		Utils.checkNotNegative(id, "id");
		return categoryMapper.selectById(id);
	}

	/**
	 * 获取指定分类往下的第 N 级分类。
	 * N=1 表示子分类， N=2 表示子分类的子分类，以此类推。
	 *
	 * @param id    分类 ID
	 * @param depth 向下级数 N
	 */
	public List<Category> findSubLayer(int id, int depth) {
		Utils.checkNotNegative(id, "id");
		Utils.checkPositive(depth, "depth");
		return categoryMapper.selectSubLayer(id, depth);
	}

	/**
	 * 查出指定 ID 的分类的所有下级分类。
	 *
	 * @param ancestor 根分类的 ID
	 * @return 分类列表
	 */
	public List<Category> findTree(int ancestor) {
		Utils.checkNotNegative(ancestor, "ancestor");
		return categoryMapper.selectDescendant(ancestor);
	}

	/**
	 * 查出指定 ID 的分类的所有下级分类。
	 *
	 * @param ancestor 根分类的 ID
	 * @param limit 深度限制
	 * @return 分类列表
	 */
	public List<Category> findTree(int ancestor, int limit) {
		Utils.checkNotNegative(ancestor, "ancestor");
		return categoryMapper.selectDescendant(ancestor, limit);
	}

	/**
	 * 获取根分类到此分类（含）路径上的所有的分类对象。
	 * 如果指定的分类不存在，则返回空列表。
	 *
	 * @return 分类列表，越上级的分类在列表中的位置越靠前
	 */
	public List<Category> findPath(int id) {
		return categoryMapper.selectPathToRoot(id);
	}

	/**
	 * 获取指定分类（含）到其某个的上级分类（不含）之间的所有分类的对象。
	 * 如果指定的分类、上级分类不存在，或是上级分类不是指定分类的上级，则返回空列表
	 *
	 * @param ancestor 上级分类的 ID，若为 0 则表示获取到一级分类（含）的列表。
	 * @param descendant 上级分类的 ID，若为 0 则表示获取到一级分类（含）的列表。
	 * @return 分类列表，越靠上的分类在列表中的位置越靠前。
	 * @throws IllegalArgumentException 如果 ancestor 小于1。
	 */
	public List<Category> findBetween(int ancestor, int descendant) {
		Utils.checkNotNegative(ancestor, "ancestor");
		Utils.checkPositive(ancestor, "descendant");
		return categoryMapper.selectPathToAncestor(descendant, ancestor);
	}

	/**
	 * 获取所有分类的数量，不包括根分类。
	 *
	 * @return 分类数量
	 */
	public int size() {
		return categoryMapper.selectCount() - 1;
	}

	/**
	 * 新增一个分类，自动生成并设置 ID 属性。
	 *
	 * @param category 分类实体对象
	 * @param parent   上级分类id
	 */
	public void add(Category category, int parent) {
		if (parent > 0 && categoryMapper.contains(parent) == null) {
			throw new IllegalArgumentException("上级分类不存在");
		}
		try {
			categoryMapper.insert(category);
			categoryMapper.insertPath(category.getId(), parent);
			categoryMapper.insertSelfLink(category.getId());
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
		try {
			Utils.checkEffective(categoryMapper.update(category));
		} catch (PersistenceException ex) {
			throw new IllegalArgumentException(ex);
		}
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

		if (categoryMapper.contains(id) == null) {
			throw new IllegalArgumentException("指定的分类不存在");
		}
		var parent = categoryMapper.selectAncestor(id, 1);
		if (parent == null) {
			parent = 0;
		}
		findById(id).moveSubTree(parent);
		deleteBoth(id);
	}

	/**
	 * 删除一个分类及其所有的下级分类，顶级分类不可删除。
	 *
	 * @param id 要删除的分类的 ID
	 * @throws IllegalArgumentException 如果指定 ID 的分类不存在
	 */
	public void deleteTree(int id) {
		Utils.checkPositive(id, "id");

		if (categoryMapper.contains(id) == null) {
			throw new IllegalArgumentException("指定的分类不存在");
		}
		deleteBoth(id);
		for (var des : categoryMapper.selectDescendantId(id)) {
			deleteBoth(des);
		}
	}

	/**
	 * 删除两个表中与分类相关的记录。
	 *
	 * @param id 分类的 ID
	 */
	private void deleteBoth(int id) {
		Utils.checkEffective(categoryMapper.delete(id));
		categoryMapper.deletePath(id);
	}
}
