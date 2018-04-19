package net.kaciras.example;

import org.apache.ibatis.exceptions.PersistenceException;

import java.util.List;

/**
 * 基于ClosureTable的的数据库存储分类树实现。
 *
 * @author Kaciras
 */
public class ClosureTableCategoryStore implements CategoryStore {

	private final CategoryMapper categoryMapper;

	public ClosureTableCategoryStore(CategoryMapper categoryMapper) {
		this.categoryMapper = categoryMapper;
	}

	private void assertContains(int id) {
		Boolean v = categoryMapper.contains(id);
		if(v == null || !v) {
			throw new IllegalArgumentException("指定的分类不存在");
		}
	}

	@Override
	public Category get(int id) {
		Utils.checkPositive(id, "id");
		return categoryMapper.selectAttributes(id);
	}

	@Override
	public int getCount() {
		return categoryMapper.selectCount();
	}

	@Override
	public int getCount(int layer) {
		Utils.checkPositive(layer, "layer");
		return categoryMapper.selectCountByLayer(layer);
	}

	@Override
	public Category getParent(int id) {
		return getAncestor(id, 1);
	}

	@Override
	public Category getAncestor(int id, int distant) {
		Utils.checkPositive(id, "id");
		Utils.checkPositive(distant, "distant");
		Integer parent = categoryMapper.selectAncestor(id, distant);
		return parent == null ? null : get(parent);
	}

	@Override
	public List<Category> getPath(int id) {
		Utils.checkPositive(id, "id");
		return categoryMapper.selectPathToRoot(id);
	}

	@Override
	public List<Category> getPath(int id, int ancestor) {
		Utils.checkPositive(id, "id");
		Utils.checkPositive(ancestor, "ancestor");
		return categoryMapper.selectPathToAncestor(id, ancestor);
	}

	@Override
	public int getLevel(int id) {
		Utils.checkPositive(id, "id");
		return Utils.notNull(categoryMapper.selectDistance(0, id));
	}

	@Override
	public List<Category> getSubCategories(int id) {
		return getSubCategories(id, 1);
	}

	@Override
	public List<Category> getSubCategories(int id, int n) {
		Utils.checkNotNegative(id, "id");
		Utils.checkPositive(n, "n");
		return categoryMapper.selectSubLayer(id, n);
	}

	@Override
	public int[] getDescendant(int ancestor) {
		Utils.checkNotNegative(ancestor, "ancestor");
		return categoryMapper.selectDescendant(ancestor);
	}

	@Override
	public int add(Category category, int parent) {
		Utils.checkNotNegative(parent, "parent");
		if(parent > 0) {
			assertContains(parent);
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

	@Override
	public void move(int id, int target) {
		if(id == target) {
			throw new IllegalArgumentException("不能移动到自己下面");
		}
		Utils.checkNotNegative(target, "target");
		assertContains(id);
		if(target > 0) {
			assertContains(target);
		}

		moveSubTree(id, categoryMapper.selectAncestor(id, 1));
		moveNode(id, target);
	}

	@Override
	public void moveTree(int id, int target) {
		Utils.checkNotNegative(target, "target");
		assertContains(id);
		if(target > 0) {
			assertContains(target);
		}

		/* 移动分移到自己子树下和无关节点下两种情况 */
		Integer distance = categoryMapper.selectDistance(id, target);
		if (distance == null) {
			// 移动到父节点或其他无关系节点，不需要做额外动作
		} else if (distance == 0) {
			throw new IllegalArgumentException("不能移动到自己下面");
		} else {
			// 如果移动的目标是其子类，需要先把子类移动到本类的位置
			int parent = categoryMapper.selectAncestor(id, 1);
			moveNode(target, parent);
			moveSubTree(target, target);
		}

		moveNode(id, target);
		moveSubTree(id, id);
	}

	/**
	 * 将指定节点移动到另某节点下面，该方法不修改子节点的相关记录，
	 * 为了保证数据的完整性，需要与moveSubTree()方法配合使用。
	 *
	 * @param id 指定节点id
	 * @param parent 某节点id
	 */
	private void moveNode(int id, int parent) {
		categoryMapper.deletePath(id);
		categoryMapper.insertPath(id, parent);
		categoryMapper.insertNode(id);
	}

	/**
	 * 将指定节点的所有子树移动到某节点下
	 * 如果两个参数相同，则相当于重建子树，用于父节点移动后更新路径
	 *
	 * @param id     指定节点id
	 * @param parent 某节点id
	 */
	private void moveSubTree(int id, int parent) {
		int[] subs = categoryMapper.selectSubId(id);
		for (int sub : subs) {
			moveNode(sub, parent);
			moveSubTree(sub, sub);
		}
	}

	/**
	 * 该方法仅更新分类的属性，不修改继承关系，若要移动节点请使用
	 * <code>move</code>和<code>moveTree</code>
	 *
	 * @param category 新的分类信息对象
	 */
	@Override
	public void update(Category category) {
		try {
			Utils.checkEffective(categoryMapper.update(category));
		}catch (PersistenceException ex) {
			throw new IllegalArgumentException(ex);
		}
	}

	@Override
	public void delete(int id) {
		assertContains(id);
		Integer parent = categoryMapper.selectAncestor(id, 1);
		if (parent == null) {
			parent = 0;
		}
		moveSubTree(id, parent);
		deleteBoth(id);
	}

	@Override
	public void deleteTree(int id) {
		assertContains(id);
		deleteBoth(id);
		for (int des : categoryMapper.selectDescendant(id)) {
			deleteBoth(des);
		}
	}

	/**
	 * 删除一个分类，两个表中的相关记录都删除
	 *
	 * @param id 分类id
	 */
	private void deleteBoth(int id) {
		categoryMapper.delete(id);
		categoryMapper.deletePath(id);
	}
}
