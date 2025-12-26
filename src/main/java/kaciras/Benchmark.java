package kaciras;

import kaciras.setup.AreaCodeDataset;
import kaciras.setup.DBManager;
import lombok.Cleanup;

import java.sql.Connection;

public final class Benchmark {

	public static void run() throws Exception {
		var manager = DBManager.open();

		// 建表和导入数据
		try (
				var closure = manager.createTable("closure.sql");
				var adjacent = manager.createTable("adjacent.sql");
				var ds = new AreaCodeDataset()
		) {
			while (ds.hasNext()) {
				var entry = ds.next();
				closure.importData(entry);
				adjacent.importData(entry);
			}
		}

		Runtime.getRuntime().addShutdownHook(new Thread(manager::dropTables));

		System.out.println("开始测试性能");
		@Cleanup var conn = manager.getDataSource().getConnection();

		bench(conn, "邻接表用时(ms): ", 1000, """
						WITH RECURSIVE temp(p, n) AS (
						     SELECT id,`name` FROM adjacent WHERE id=130100000000
						     UNION
						     SELECT id,`name` FROM adjacent, temp
						     WHERE adjacent.parent=temp.p
						)
						SELECT * FROM temp;
				""");
		bench(conn, "闭包表用时(ms): ", 1000, "SELECT id,name FROM category JOIN category_tree ON id=descendant WHERE ancestor=130100000000");
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
