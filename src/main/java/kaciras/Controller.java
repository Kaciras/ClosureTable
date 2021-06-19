package kaciras;

import lombok.RequiredArgsConstructor;

import java.util.List;

// 闭包表支持的操作很多，这里只选了一些主要的进行演示。
@SuppressWarnings("unused")
@RequiredArgsConstructor
public final class Controller {

	private final Repository repository;

	public List<Category> getAll() {
		return repository.findById(0).getTree();
	}

	public Category create(int parentId, String name) {
		var category = new Category();
		category.setName(name);
		category.setParentId(parentId);
		repository.add(category);
		return category;
	}

	public void update(int id, String newName) {
		var category = new Category();
		category.setId(id);
		category.setName(newName);
		repository.update(category);
	}

	public int getLevel(int id) {
		return repository.findById(id).getLevel();
	}

	public void move(int id, int parent, boolean single) {
		var category = repository.findById(id);
		var newParent =  repository.findById(parent);

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
