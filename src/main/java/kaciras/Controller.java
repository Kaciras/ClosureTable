package kaciras;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public final class Controller {

	private final Repository repository;

	public List<Category> getAll() {
		return null;
	}

	public Category create(int parentId, String name) {
		var category = new Category();
		category.setName(name);
		repository.add(category, parentId);
		return category;
	}

	public void move(int id, int newParent) {
		repository.findById(id).moveTo(newParent);
	}

	public void moveTree(int id, int newParent) {
		repository.findById(id).moveTreeTo(newParent);
	}

	public void update(int id, String newName) {
		var category = new Category();
		category.setId(id);
		category.setName(newName);
		repository.update(category);
	}

	public void delete(int id) {
		repository.delete(id);
	}

	public void deleteTree(int id) {
		repository.deleteTree(id);
	}

	public List<Category> getPath(int id, int ancestor) {
		return repository.findById(id).getPathRelativeTo(ancestor);
	}

	public int getLevel(int id) {
		return repository.findById(id).getLevel();
	}
}
