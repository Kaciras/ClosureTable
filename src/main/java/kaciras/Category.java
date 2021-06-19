package kaciras;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;

/**
 * 分类对象，该类使用充血模型，除了属性之外还包含了一些方法。
 * 不过本项目里没有什么与分类自身相关的逻辑，分类之间的逻辑也放到 Repository 里了。
 */
@EqualsAndHashCode(of = "id")
@Data
@NoArgsConstructor
public class Category {

	/*
	 * 一些方法需要依赖 CategoryMapper，在调用方法前需要将其注入此类。
	 * 如果用 Spring 则可以靠 @Configurable 来完成。
	 */
	static CategoryMapper mapper;

	/** 分类的 ID，由数据库生成 */
	private int id;

	/** 分类名 */
	private String name;

	/** 父类的 ID */
	private Integer parentId;

	/**
	 * 查询指定分类往上第 N 级分类，如果不存在则返回 null。
	 * N=0 返回自身的 ID，N=1 返回父 ID，以此类推。
	 *
	 * @param n 距离
	 * @return 上级分类的 ID
	 */
	public Integer getAncestorId(int n) {
		Utils.checkPositive(n, "n");
		return mapper.selectAncestor(id, n);
	}

	/**
	 * 查询分类是哪一级的，根分类级别是 0。
	 *
	 * @return 级别
	 */
	public int getLevel() {
		return mapper.selectDistance(id, 0);
	}

	/**
	 * 获取该分类往下的第 N 级分类。
	 * N=1 表示子分类， N=2 表示子分类的子分类，以此类推。
	 *
	 * @param depth 向下级数 N
	 */
	public List<Category> getSubLayer(int depth) {
		Utils.checkPositive(depth, "depth");
		return mapper.selectSubLayer(id, depth);
	}

	public List<Category> getChildren() {
		return mapper.selectSubLayer(id, 1);
	}

	/**
	 * 获取该分类的所有下级分类（），返回结果的顺序不做保证。
	 */
	public List<Category> getTree() {
		return mapper.selectDescendant(id);
	}

	/**
	 * 获取根分类到此分类（含）路径上的所有的分类对象。
	 * 如果指定的分类不存在，则返回空列表。
	 *
	 * @return 分类列表，越上级的分类在列表中的位置越靠前
	 */
	public List<Category> getPath() {
		return mapper.selectPathToRoot(id);
	}

	/**
	 * 获取指定分类（含）到其某个的上级分类（不含）之间的所有分类的对象。
	 * 如果指定的分类、上级分类不存在，或是上级分类不是指定分类的上级，则返回空列表
	 *
	 * @param ancestor 上级分类的 ID，若为 0 则表示获取到一级分类（含）的列表。
	 * @return 分类列表，越靠上的分类在列表中的位置越靠前。
	 * @throws IllegalArgumentException 如果 ancestor 小于1。
	 */
	public List<Category> getPath(Category ancestor) {
		return mapper.selectPathToAncestor(id, Objects.requireNonNull(ancestor).id);
	}

	/**
	 * 将一个分类移动到目标分类下面（成为其子分类）。被移动分类的子类将自动上浮
	 * （成为指定分类父类的子分类），即使目标是指定分类原本的父类。
	 * <p>
	 * 例如下图（省略根分类）：
	 * <pre>
	 *       1                                    1
	 *       |                                  / | \
	 *       2                                 3  4  5
	 *     / | \         (id=2).moveTo(7)           / \
	 *    3  4  5       ----------------->         6   7
	 *         / \                                /  / | \
	 *       6    7                              8  9  10 2
	 *      /    /  \
	 *     8    9    10
	 * </pre>
	 *
	 * @param newParent 目标分类的id
	 * @throws IllegalArgumentException 如果 target 所表示的分类不存在或是自身
	 */
	public void moveTo(Category newParent) {
		if (id == 0) {
			throw new UnsupportedOperationException("根分类不支持此操作");
		}

		var target = Objects.requireNonNull(newParent).id;
		if (id == target) {
			throw new IllegalArgumentException("不能移动到自己下面");
		}

		moveSubTree(id, mapper.selectAncestor(id, 1));
		moveNode(id, target);
	}

	/**
	 * 将一个分类移动到目标分类下面（成为其子分类），被移动分类的子分类也会随着移动。
	 * 如果目标分类是被移动分类的子类，则先将目标分类（连带子类）移动到被移动分类原来的
	 * 的位置，再移动需要被移动的分类。
	 * <p>
	 * 例如下图（省略根分类）：
	 * <pre>
	 *       1                                      1
	 *       |                                      |
	 *       2                                      7
	 *     / | \        (id=2).moveTreeTo(7)      / | \
	 *    3  4  5      -------------------->     9  10  2
	 *         / \                                  / | \
	 *       6    7                                3  4  5
	 *      /    /  \                                    |
	 *     8    9    10                                  6
	 *                                                   |
	 *                                                   8
	 * </pre>
	 *
	 * @param newParent 目标分类的
	 * @throws IllegalArgumentException 如果 target 所表示的分类不存在或是自身
	 */
	public void moveTreeTo(Category newParent) {
		if (id == 0) {
			throw new UnsupportedOperationException("根分类不支持此操作");
		}

		// 移动分移到自己子树下和无关节点下两种情况
		var target = Objects.requireNonNull(newParent).id;
		var distance = mapper.selectDistance(target, id);

		// noinspection StatementWithEmptyBody
		if (distance == null) {
			// 移动到父节点或其他无关系节点，不需要做额外动作
		} else if (distance == 0) {
			throw new IllegalArgumentException("不能移动到自己下面");
		} else {
			// 如果移动的目标是其子类，需要先把子类移动到本类的位置
			int parent = mapper.selectAncestor(id, 1);
			moveNode(target, parent);
			moveSubTree(target, target);
		}

		moveNode(id, target);
		moveSubTree(id, id);
	}

	/**
	 * 将指定节点移动到另某节点下面，该方法不修改子节点的相关记录，
	 * 为了保证数据的完整性，需要与 moveSubTree() 方法配合使用。
	 *
	 * @param id     指定节点 ID
	 * @param parent 某节点 ID
	 */
	private void moveNode(int id, int parent) {
		mapper.deletePath(id);
		mapper.insertPath(id, parent);
		mapper.insertSelfLink(id);
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
		var subs = mapper.selectSubId(id);
		for (int sub : subs) {
			moveNode(sub, parent);
			moveSubTree(sub, sub);
		}
	}
}
