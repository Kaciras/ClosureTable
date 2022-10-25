package kaciras;

import lombok.Cleanup;
import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

/**
 * 控制器类，包含演示页面所使用的 API。
 * <p>
 * 闭包表支持的操作很多，这里只选了一些有代表性的进行演示。
 */
@SuppressWarnings("unused")
@RequiredArgsConstructor
public final class Controller {

	private final DataSource dataSource;
	private final Repository repository;

	public String getDatabaseName() throws SQLException {
		try (var connection = dataSource.getConnection()) {
			var meta = connection.getMetaData();
			var name = meta.getDatabaseProductName();
			var version = meta.getDatabaseProductVersion();
			return name + " - " + version;
		}
	}

	public List<ListQueryVO> getAll() {
		return repository.getAllForDemo();
	}

	public Category create(int parentId, String name) {
		var category = new Category();
		category.setName(name);
		repository.add(category, repository.findById(parentId));
		return category;
	}

	public void update(int id, String newName) {
		var category = repository.findById(id);
		category.setName(newName);
		repository.update(category);
	}

	public int getLevel(int id) {
		return repository.findById(id).getLevel();
	}

	public void move(int id, int parent, boolean single) {
		var category = repository.findById(id);
		var newParent = repository.findById(parent);

		if (single) {
			category.moveTo(newParent);
		} else {
			category.moveTreeTo(newParent);
		}
	}

	public void delete(int id, boolean single) {
		if (single) {
			repository.delete(id);
		} else {
			repository.deleteTree(id);
		}
	}

	public List<Category> getPath(int ancestor, int descendant) {
		var ans = repository.findById(ancestor);
		return repository.findById(descendant).getPath(ans);
	}

	public List<Category> getTree(int id) {
		return repository.findById(id).getTree();
	}

	public List<Category> getSubLayer(int id, int depth) {
		return repository.findById(id).getSubLayer(depth);
	}
}
