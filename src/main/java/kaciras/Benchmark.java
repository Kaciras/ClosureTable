package kaciras;

import kaciras.setup.AreaCodeDataset;
import kaciras.setup.DBManager;
import lombok.Cleanup;
import me.tongfei.progressbar.ProgressBar;

import java.sql.Connection;

public final class Benchmark {

	public static void run() throws Exception {
		var manager = DBManager.open();
		var connection = manager.getConnection();

		if (!manager.tableExists("adjacent")) {
			initialize(manager);
		} else {
			System.out.println("检测到相关表已经存在，使用现有的数据");
		}

		System.out.println("查询所有下级节点。");
		bench(connection, "邻接表用时(ms): ", 1000, """
					WITH RECURSIVE temp(p, n) AS (
					     SELECT id,`name` FROM adjacent WHERE id=130100000000
					     UNION
					     SELECT id,`name` FROM adjacent, temp
					     WHERE adjacent.parent=temp.p
					)
					SELECT * FROM temp;
				""");
		bench(connection, "闭包表用时 (ms): ", 1000, "SELECT id,name FROM category JOIN category_tree ON id=descendant WHERE ancestor=130100000000");

		System.out.println("\n测试结束，表和数据未删除。");
	}

	static void initialize(DBManager manager) throws Exception {
		try (
				var adjacent = manager.createTable("adjacent.sql");
				var closure = manager.createTable("closure.sql");
				var ds = new AreaCodeDataset();
				var pb = new ProgressBar("导入数据", ds.getTotal())
		) {
			while (ds.hasNext()) {
				var entry = ds.next();
				pb.stepTo(ds.getProgress());
				closure.importData(entry);
				adjacent.importData(entry);
			}
		}
	}

	private static void bench(Connection conn, String name, int times, String sql) throws Exception {
		@Cleanup var stat = conn.createStatement();

		var start = System.currentTimeMillis();
		for (int i = 0; i < times; i++) {
			stat.execute(sql);
		}
		var end = System.currentTimeMillis();

		var time = (end - start) / (double) times;
		System.out.printf("%s: %.3f%n", name, time);
	}
}
