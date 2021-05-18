package kaciras;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分类对象，该类使用充血模型，除了属性之外还包含了一些方法。
 *
 * @author kaciras
 */
@EqualsAndHashCode(of = "id")
@Data
@NoArgsConstructor
public class Category {

	/*
	 * 一些方法需要依赖 CategoryMapper，在调用方法前需要将其注入此类。
	 * 如果用 Spring 则可以靠 @Configurable 来完成。
	 */
	static CategoryMapper categoryMapper;

	/** 分类id，数据库生成 */
	private int id;

	/** 分类名 */
	private String name;

	/**
	 * 获取分类的父分类。
	 *
	 * @return 父分类实体对象，如果指定的分类是一级分类，则返回null
	 */
	public Category getParent() {
		return getAncestor(1);
	}

	/**
	 * 查询指定分类往上第n级分类。
	 *
	 * @param n 距离
	 * @return 上级分类，如果不存在则为null
	 */
	public Category getAncestor(int n) {
		Utils.checkPositive(n, "distant");
		var parent = categoryMapper.selectAncestor(id, n);
		return parent == null ? null : categoryMapper.selectAttributes(parent);
	}

	/**
	 * 分类的下的直属子分类。
	 *
	 * @return 直属子类列表，如果id所指定的分类不存在、或没有符合条件的分类，则返回空列表
	 * @throws IllegalArgumentException 如果id小于0
	 */
	public List<Category> getChildren() {
		return getChildren(1);
	}

	/**
	 * 分类的下的第n级子分类。
	 *
	 * @param n 向下级数，1表示直属子分类
	 * @return 子类列表，如果id所指定的分类不存在、或没有符合条件的分类，则返回空列表
	 * @throws IllegalArgumentException 如果id小于0，或n不是正数
	 */
	public List<Category> getChildren(int n) {
		Utils.checkNotNegative(id, "id");
		Utils.checkPositive(n, "n");
		return categoryMapper.selectSubLayer(id, n);
	}

	/**
	 * 获取由顶级分类到此分类(含)路径上的所有的分类对象。
	 * 如果指定的分类不存在，则返回空列表。
	 *
	 * @return 分类实体列表，越靠上的分类在列表中的位置越靠前
	 */
	public List<Category> getPath() {
		return categoryMapper.selectPathToRoot(id);
	}

	/**
	 * 获取此分类(含)到其某个的上级分类（不含）之间的所有分类的实体对象（仅查询id和name属性）。
	 * 如果指定的分类、上级分类不存在，或是上级分类不是指定分类的上级，则返回空列表
	 *
	 * @param ancestor 上级分类的id，若为0则表示获取到一级分类（含）的列表。
	 * @return 分类实体列表，越靠上的分类在列表中的位置越靠前。
	 * @throws IllegalArgumentException 如果ancestor小于1。
	 */
	public List<Category> getPathRelativeTo(int ancestor) {
		Utils.checkPositive(ancestor, "ancestor");
		return categoryMapper.selectPathToAncestor(id, ancestor);
	}

	/**
	 * 查询分类是哪一级的，根分类级别是0。
	 *
	 * @return 级别
	 */
	int getLevel() {
		return categoryMapper.selectDistance(0, id);
	}

	/**
	 * 将一个分类移动到目标分类下面（成为其子分类）。被移动分类的子类将自动上浮（成为指定分类
	 * 父类的子分类），即使目标是指定分类原本的父类。
	 * <p>
	 * 例如下图(省略顶级分类)：
	 * 1                                    1
	 * |                                  / | \
	 * 2                                 3  4  5
	 * / | \         (id=2).moveTo(7)           / \
	 * 3  4  5       ----------------->         6   7
	 * / \                                /  / | \
	 * 6    7                              8  9  10 2
	 * /    /  \
	 * 8    9    10
	 *
	 * @param target 目标分类的id
	 * @throws IllegalArgumentException 如果target所表示的分类不存在、或此分类的id==target
	 */
	public void moveTo(int target) {
		if (id == target) {
			throw new IllegalArgumentException("不能移动到自己下面");
		}

		Utils.checkNotNegative(target, "target");
		if (target > 0 && categoryMapper.contains(target) == null) {
			throw new IllegalArgumentException("指定的上级分类不存在");
		}

		moveSubTree(id, categoryMapper.selectAncestor(id, 1));
		moveNode(id, target);
	}

	/**
	 * 将一个分类移动到目标分类下面（成为其子分类），被移动分类的子分类也会随着移动。
	 * 如果目标分类是被移动分类的子类，则先将目标分类（连带子类）移动到被移动分类原来的
	 * 的位置，再移动需要被移动的分类。
	 * <p>
	 * 例如下图(省略顶级分类)：
	 * 1                                      1
	 * |                                      |
	 * 2                                      7
	 * / | \        (id=2).moveTreeTo(7)      / | \
	 * 3  4  5      -------------------->     9  10  2
	 * / \                                  / | \
	 * 6    7                                3  4  5
	 * /    /  \                                    |
	 * 8    9    10                                  6
	 * |
	 * 8
	 *
	 * @param target 目标分类的id
	 * @throws IllegalArgumentException 如果id或target所表示的分类不存在、或id==target
	 */
	public void moveTreeTo(int target) {
		Utils.checkNotNegative(target, "target");
		if (target > 0 && categoryMapper.contains(target) == null) {
			throw new IllegalArgumentException("指定的上级分类不存在");
		}

		/* 移动分移到自己子树下和无关节点下两种情况 */
		var distance = categoryMapper.selectDistance(id, target);

		// noinspection StatementWithEmptyBody
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
	 * @param id     指定节点id
	 * @param parent 某节点id
	 */
	private void moveNode(int id, int parent) {
		categoryMapper.deletePath(id);
		categoryMapper.insertPath(id, parent);
		categoryMapper.insertNode(id);
	}

	void moveSubTree(int parent) {
		moveSubTree(id, parent);
	}

	/**
	 * 将指定节点的所有子树移动到某节点下
	 * 如果两个参数相同，则相当于重建子树，用于父节点移动后更新路径
	 *
	 * @param id     指定节点id
	 * @param parent 某节点id
	 */
	private void moveSubTree(int id, int parent) {
		var subs = categoryMapper.selectSubId(id);
		for (int sub : subs) {
			moveNode(sub, parent);
			moveSubTree(sub, sub);
		}
	}
}
