package net.kaciras.example;

/**
 * 根分类，覆盖了分类的一些方法，防止这些操作被错误地用到顶级分类上。
 */
final class RootCategory extends Category {

	@Override
	public void moveTo(int target) {
		throw new UnsupportedOperationException("根分类不支持此操作");
	}

	@Override
	public void moveTreeTo(int target) {
		throw new UnsupportedOperationException("根分类不支持此操作");
	}
}
