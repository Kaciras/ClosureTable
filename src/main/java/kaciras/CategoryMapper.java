package kaciras;

import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 基于ClosureTable的数结构存储。
 *
 * 数据库使用Mysql(Mariadb 10.2.9)，如果你的不是mysql，insertPath()方法的语句可能需要修改
 * @author Kaciras
 */
@Mapper
public interface CategoryMapper {

	/* - - - - - - - - - - - - - - - - - - - - - *\
	       下面几个没注释的是最基本的增删改查
	\* - - - - - - - - - - - - - - - - - - - - - */

	@Select("SELECT * FROM category WHERE id=#{id}")
	@TypeDiscriminator(column = "id", javaType = int.class, cases = @Case(value = "0", type = RootCategory.class))
	Category selectAttributes(int id);

	@Select("SELECT COUNT(*) FROM category")
	int selectCount();

	@Update("UPDATE category SET `name`=#{name} WHERE id=#{id}")
	int update(Category category);

	@Insert("INSERT INTO category(name) VALUES(#{name})")
	@Options(useGeneratedKeys=true, keyProperty = "id", keyColumn="id")
	void insert(Category entity);

	@Delete("DELETE FROM category WHERE id=#{id}")
	int delete(int id);

	/**
	 * 查询某一层的节点的数量。
	 *
	 * @param level 层级
	 * @return 节点数量
	 */
	@Select("SELECT COUNT(*) FROM category_tree WHERE ancestor=0 AND distance=#{level}")
	int selectCountByLayer(int level);

	/**
	 * 查询某个节点的子树中所有的节点，不包括参数所指定的节点。
	 *
	 * @param id 节点id
	 * @return 子节点
	 */
	@Select("SELECT B.* FROM category_tree AS A " +
			"JOIN category AS B ON A.descendant=B.id " +
			"WHERE ancestor=#{id} AND distance>0")
	List<Category> selectDescendant(int id);

	/**
	 * 查询某个节点的子树中所有的节点的 id，不包括参数所指定的节点。
	 *
	 * @param id 节点id
	 * @return 子节点id
	 */
	@Select("SELECT descendant FROM category_tree WHERE ancestor=#{id} AND distance>0")
	int[] selectDescendantId(int id);

	/**
	 * 查询某个节点的第 N 级子节点。
	 *
	 * @param ancestor 祖先节点ID
	 * @param n 距离（0表示自己，1表示直属子节点）
	 * @return 子节点列表
	 */
	@Select("SELECT B.* FROM category_tree AS A " +
			"JOIN category AS B ON A.descendant=B.id " +
			"WHERE A.ancestor=#{ancestor} AND A.distance=#{n}")
	List<Category> selectSubLayer(int ancestor, int n);

	/**
	 * 查询某个节点的第 N 级父节点。如果 id 指定的节点不存在、操作错误或是数据库被外部修改，
	 * 则可能查询不到父节点，此时返回 null。
	 *
	 * @param id 节点id
	 * @param n 祖先距离（0表示自己，1表示直属父节点）
	 * @return 父节点id，如果不存在则返回 null
	 */
	@Select("SELECT ancestor FROM category_tree WHERE descendant=#{id} AND distance=#{n}")
	Integer selectAncestor(int id, int n);

	/**
	 * 查询由 id 指定节点(含)到根节点(不含)的路径。
	 * 比下面的<code>selectPathToAncestor</code>简单些。
	 *
	 * @param id 节点ID
	 * @return 路径列表。如果节点不存在，则返回空列表
	 */
	@Select("SELECT B.* FROM category_tree AS A " +
			"JOIN category AS B ON A.ancestor=B.id " +
			"WHERE descendant=#{id} AND ancestor>0 ORDER BY distance DESC")
	List<Category> selectPathToRoot(int id);

	/**
	 * 查询由 id 指定节点(含)到指定上级节点(不含)的路径。
	 *
	 * @param id 节点ID
	 * @param ancestor 上级节点的ID
	 * @return 路径列表。如果节点不存在，或上级节点不存在，则返回空列表
	 */
	@Select("SELECT B.* FROM category_tree AS A " +
			"JOIN category AS B ON A.ancestor=B.id " +
			"WHERE descendant=#{id} AND " +
			"distance<(SELECT distance FROM category_tree WHERE descendant=#{id} AND ancestor=#{ancestor}) " +
			"ORDER BY distance DESC")
	List<Category> selectPathToAncestor(int id, int ancestor);

	/**
	 * 查找某节点下的所有直属子节点的 id。
	 * 该方法与上面的<code>selectSubLayer</code>不同，它只查询节点的 id，效率高点。
	 *
	 * @param parent 分类id
	 * @return 子类id数组
	 */
	@Select("SELECT descendant FROM category_tree WHERE ancestor=#{parent} AND distance=1")
	int[] selectSubId(int parent);

	/**
	 * 查询某节点到它某个祖先节点的距离。
	 *
	 * @param ancestor 父节点id
	 * @param id 节点id
	 * @return 距离（0表示到自己的距离）,如果ancestor并不是其祖先节点则返回null
	 */
	@Select("SELECT distance FROM category_tree WHERE descendant=#{id} AND ancestor=#{ancestor}")
	Integer selectDistance(int ancestor, int id);

	/**
	 * 复制父节点的路径结构,并修改 descendant 和 distance
	 *
	 * @param id 节点id
	 * @param parent 父节点id
	 */
	@Insert("INSERT INTO category_tree(ancestor,descendant,distance) " +
			"(SELECT ancestor,#{id},distance+1 FROM category_tree WHERE descendant=#{parent})")
	void insertPath(int id, int parent);

	/**
	 * 在关系表中插入对自身的连接。
	 *
	 * @param id 节点id
	 */
	@Insert("INSERT INTO category_tree(ancestor,descendant,distance) VALUES(#{id},#{id},0)")
	void insertNode(int id);

	/**
	 * 从树中删除某节点的路径。注意指定的节点可能存在子树，而子树的节点在该节点之上的路径并没有改变，
	 * 所以使用该方法后还必须手动修改子节点的路径以确保树的正确性。
	 *
	 * @param id 节点id
	 */
	@Delete("DELETE FROM category_tree WHERE descendant=#{id}")
	void deletePath(int id);

	/**
	 * 判断分类是否存在。
	 *
	 * @param id 分类id
	 * @return true 表示存在，null 或 false 表示不存在
	 */
	@Select("SELECT 1 FROM category WHERE id=#{id}")
	Boolean contains(int id);
}
