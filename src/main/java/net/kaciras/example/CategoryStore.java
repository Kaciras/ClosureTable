package net.kaciras.example;

import java.util.List;

/**
 * 分类存储，提供对分类的增删改查等操作的支持。
 *
 * 类别（分类）是用于归类、整理文章资源的数据信息。
 * 每个分类都可以拥有若干子分类，但最多只能属于一个父分类。没有父分类的称为顶级分类。
 * 分类的从属关系可以看做一棵多叉数。
 *
 * 除了相互之间的关系外，分类拥有ID、名称、简介、封面四个属性。其中ID为int，
 * 由数据库自动生成。
 *
 * 分类树的根节点成为顶级分类，其ID为0，不可修改、移动、删除或查询其属性，
 * 也不会出现在批量查询的结果集中。
 * 顶级分类仅作为对一些与继承关系有关的参数，例如addNew方法中parent参数为0表示添
 * 加为一级分类。
 *
 * @author Kaciras
 */
interface CategoryStore {

	/**
	 * 根据指定的id，获取分类的全部属性。
	 *
	 * @param id 分类id
	 * @return 分类的实体对象
	 * @throws IllegalArgumentException 如果id不是正数
	 */
	Category get(int id);

	/**
	 * 获取所有分类的数量
	 * @return 数量
	 */
	int getCount();

	/**
	 * 获取某一级分类的数量
	 * @param layer 层级（从1开始）
	 * @return 数量
	 * @throws IllegalArgumentException 如果layer不是正数
	 */
	int getCount(int layer);

	/**
	 * 获取指定分类的父分类。
	 *
	 * @param id 指定分类的id
	 * @return 父分类实体对象，如果指定的分类是一级分类，则返回null
	 * @throws IllegalArgumentException 如果id不是正数
	 */
	Category getParent(int id);

	/**
	 * 查询指定分类往上第n级分类。
	 *
	 * @param id 指定分类的id
	 * @param n 距离
	 * @return 分类实体对象，如果没有则返回null
	 */
	Category getAncestor(int id, int n);

	/**
	 * 获取由顶级分类（不含）到指定id的分类(含)路径上的所有分类的实体对象。
	 * 如果指定的分类不存在，则返回空列表。
	 *
	 * @param id 分类id
	 * @return 分类实体列表，越靠上的分类在列表中的位置越靠前
	 * @throws IllegalArgumentException 如果id不是正数
	 */
	List<Category> getPath(int id);

	/**
	 * 获取指定id的分类(含)到其某个的上级分类（不含）之间的所有分类的实体对象（仅查询id和name属性）。
	 * 如果指定的分类、上级分类不存在，或是上级分类不是指定分类的上级，则返回空列表
	 *
	 * @param id 分类id。
	 * @param ancestor 上级分类的id，若为0则表示获取到一级分类（含）的列表。
	 * @return 分类实体列表，越靠上的分类在列表中的位置越靠前。
	 * @throws IllegalArgumentException 如果id不是正数,或ancestor小于0。
	 */
	List<Category> getPath(int id, int ancestor);

	/**
	 * 查询分类是哪一级的。
	 *
	 * @param id 分类id
	 * @return 级别
	 */
	int getLevel(int id);

	/**
	 * 获取指定id的分类下的直属子分类，id为0表示获取所有一级分类。
	 *
	 * @param id 指定分类的id
	 * @return 直属子类列表，如果id所指定的分类不存在、或没有符合条件的分类，则返回空列表
	 * @throws IllegalArgumentException 如果id小于0
	 */
	List<Category> getSubCategories(int id);

	/**
	 * 获取指定id的分类下的第n级子分类，id参数可以为0。
	 *
	 * @param id 指定分类的id
	 * @param n 向下级数，1表示直属子分类
	 * @return 子类列表，如果id所指定的分类不存在、或没有符合条件的分类，则返回空列表
	 * @throws IllegalArgumentException 如果id小于0，或n不是正数
	 */
	List<Category> getSubCategories(int id, int n);

	/**
	 * 获取分类树中ancestor所指定分类的所有子类的id（包括非直属子类）。
	 * ancestor可以为0，表示获取所有分类的id
	 *
	 * @param ancestor 分类id
	 * @return 所有子类的id
	 * @throws IllegalArgumentException 如果ancestor小于0
	 */
	int[] getDescendant(int ancestor);

	/**
	 * 新增一个分类，其ID属性将自动生成或计算，并返回。
	 * 新增分类的继承关系由parent属性指定，parent为0表示该分类为一级分类。
	 *
	 * @param category 分类实体对象
	 * @param parent 上级分类id
	 * @return 自动生成的id
	 * @throws IllegalArgumentException 如果parent所指定的分类不存在、category为null或category中存在属性为null
	 */
	int add(Category category, int parent);

	/**
	 * 将一个分类移动到目标分类下面（成为其子分类）。被移动分类的子类将自动上浮（成为指定分类
	 * 父类的子分类），即使目标是指定分类原本的父类。
	 * <p>
	 * 例如下图(省略顶级分类)：
	 *       1                                     1
	 *       |                                   / | \
	 *       2                                  3  4  5
	 *     / | \             move(2,7)               / \
	 *    3  4  5         --------------->          6   7
	 *         / \                                 /  / | \
	 *       6    7                               8  9  10 2
	 *      /    /  \
	 *     8    9    10
	 *
	 * @param id 被移动分类的id
	 * @param target 目标分类的id
	 * @throws IllegalArgumentException 如果id或target所表示的分类不存在、或id==target
	 */
	void move(int id, int target);

	/**
	 * 将一个分类移动到目标分类下面（成为其子分类），被移动分类的子分类也会随着移动。
	 * 如果目标分类是被移动分类的子类，则先将目标分类（连带子类）移动到被移动分类原来的
	 * 的位置，再移动需要被移动的分类。
	 * <p>
	 * 例如下图(省略顶级分类)：
	 *       1                                     1
	 *       |                                     |
	 *       2                                     7
	 *     / | \           moveTree(2,7)         / | \
	 *    3  4  5         --------------->      9  10  2
	 *         / \                                   / | \
	 *       6    7                                 3  4  5
	 *      /    /  \                                     |
	 *     8    9    10                                   6
	 *                                                    |
	 *                                                    8
	 *
	 * @param id 被移动分类的id
	 * @param target 目标分类的id
	 * @throws IllegalArgumentException 如果id或target所表示的分类不存在、或id==target
	 */
	void moveTree(int id, int target);

	/**
	 * 删除一个分类，原来在该分类下的子分类将被移动到该分类的父分类中，
	 * 如果此分类是一级分类，则删除后子分类将全部成为一级分类。
	 *
	 * @param id 要删除的分类的id
	 * @throws IllegalArgumentException 如果指定id的分类不存在
	 */
	void delete(int id);

	/**
	 * 删除一个分类及其子分类。
	 *
	 * @param id 要删除的分类的id
	 * @throws IllegalArgumentException 如果指定id的分类不存在
	 */
	void deleteTree(int id);

	/**
	 * 修改一个分类的名称、简介和封面属性，该方法不改变从属关系。
	 *
	 * @param category 分类实体对象
	 * @throws IllegalArgumentException 如果指定id的分类不存在
	 */
	void update(Category category);
}
