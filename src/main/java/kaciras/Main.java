package kaciras;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.session.SqlSession;

import java.util.Scanner;

public final class Main {

	private static SqlSession session;
	private static Repository repository;

	public static void main(String[] args) throws Exception {
		Utils.disableIllegalAccessWarning();
		if (args.length != 3) {
			System.err.println("请设置数据库连接参数，例如：");
			System.err.println("\tjava -jar closure-table.jar jdbc:mariadb://localhost:3306/test root password");
			return;
		}

		try {
			session = Utils.createSqlSession("org.mariadb.jdbc.Driver", args[0], args[1], args[2]);
			CategoryMapper mapper = session.getMapper(CategoryMapper.class);
			repository = new Repository(mapper);
			Category.categoryMapper = mapper; // 如果使用Spring，可以用@Configurable来注入此依赖。
			Utils.executeScript(session.getConnection(), "table.sql");

			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				Utils.dropTables(session.getConnection());
			}));
			runDemo();
		} catch (PersistenceException e) {
			System.out.println("错误：无法连接数据库，请检查启动参数。");
		}
	}

	private static void runDemo() {
		System.out.println("基于ClosureTable的分类数据库存储演示。");
		System.out.println("关键步骤中会在句子末尾显示一个问号，此时需要按按回车键确认后继续执行。");
		System.out.println("期间可以打开数据库即时查看数据的变化。");
		System.out.println();

		waitForAccept("首先创建几个分类：物品 -> 文具 -> 苹果 -> 2B铅笔，物品 -> 水果，文具 -> 尺子？");
		int item = repository.add(new Category("物品"), 0);
		int stationery = repository.add(new Category("文具"), item);
		int apple = repository.add(new Category("苹果"), stationery);
		int ruler = repository.add(new Category("尺子"), stationery);
		int pencil = repository.add(new Category("2B铅笔"), apple);
		int fruit = repository.add(new Category("水果"), item);
		session.commit();

		System.out.println("现在数据库中有一个顶级分类：" + repository.findById(item));
		System.out.println("它有两个子类：" + repository.findById(item).getChildren());
		System.out.println("2B铅笔及其所有的上级分类：" + repository.findById(pencil).getPath());
		System.out.println();

		waitForAccept("苹果分类放错了位置，需要把它移动到水果下面？");
		repository.findById(apple).moveTo(fruit);
		session.commit();
		System.out.println("现在苹果的父类是：" + repository.findById(apple).getParent());
		System.out.println("由于苹果被挪走了，2B铅笔的父类变为：" + repository.findById(pencil).getParent());
		System.out.println("此时2B铅笔及其所有的上级分类：" + repository.findById(pencil).getPath());
		System.out.println();

		waitForAccept("现在分类系统希望把事件也包含在内，所以新建一个`事物`分类，并将`物品`分类移动到其下面？");
		int thing = repository.add(new Category("事物"), 0);
		int itemLevelBefore = repository.findById(item).getLevel();
		repository.findById(item).moveTreeTo(thing);
		session.commit();
		System.out.println("这样一来，物品的级别由" + itemLevelBefore +
				"级变为了" + repository.findById(item).getLevel() + "级分类");
		System.out.println("再看看2B铅笔及其所有的上级分类：" + repository.findById(pencil).getPath());
		System.out.println();

		waitForAccept("觉得文具分类有点多余，把它删掉？");
		repository.delete(stationery);
		session.commit();
		System.out.println("现在物品的子类变多了：" + repository.findById(item).getChildren());
		System.out.println("2B铅笔及其所有的上级分类变为：" + repository.findById(pencil).getPath());
		System.out.println("当前所有的分类数量：" + repository.count() + "个");
		System.out.println("第三级分类的数量就有：" + repository.countOfLayer(3) + "个");
		System.out.println();

		waitForAccept("刚才仅是删除了一个分类，如果把物品这个分类连带下级也删除呢？");
		repository.deleteTree(item);
		session.commit();
		System.out.println("现在分类数量只有" + repository.count() + "个了");
		System.out.println("它就是唯一的顶级分类" + repository.findById(0).getChildren());
		System.out.println("当然为了保持分类树的单根，还有一个隐藏的根分类：" + repository.findById(0));
		System.out.println();

		waitForAccept("演示到此结束，更多用法请见代码注释，以及单元测试。按回车键后将清理数据库中的表。");
	}

	private static void waitForAccept(String text) {
		System.out.println(text);
		Scanner sc = new Scanner(System.in);
		sc.nextLine();
	}
}
