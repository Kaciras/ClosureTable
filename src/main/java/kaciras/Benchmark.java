package kaciras;

import lombok.Cleanup;
import org.apache.ibatis.jdbc.ScriptRunner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;

public final class Benchmark {


	// 代码总共12位，省2位，市2位，县2位，镇3位，村街道3位。
	private static final long[] MASKS = {1, 1000, 1000000, 100000000, 10000000000L};

	public static void run() throws Exception {
		var manager = DBManager.open();
		manager.importData(Benchmark::importCT);

//		@Cleanup var conn = manager.getDataSource().getConnection();
//		var stat = conn.createStatement();
//
//		var start = System.currentTimeMillis();
//		for (int i = 0; i < 10_000; i++) {
//			stat.executeQuery("SELECT id,name FROM category JOIN category_tree ON id=descendant WHERE ancestor = 130100000000");
//		}
//		var end = System.currentTimeMillis();
//		System.out.println("闭包表用时(ms): " + (end - start));
//
//		var start2 = System.currentTimeMillis();
//		for (int i = 0; i < 10_000; i++) {
//			stat.executeQuery("""
//
//			""");
//		}
//		var end2 = System.currentTimeMillis();
//		System.out.println("邻接表用时(ms): " + (end2 - start2));
	}

	private static void importCT(ScriptRunner __, Connection connection) throws Exception {
		System.out.println("正在导入数据...");

		connection.createStatement().execute("""
				CREATE TABLE adjacent
				(
				    id     BIGINT     NOT NULL,
				    parent BIGINT     NOT NULL,
				    name   TINYTEXT   NOT NULL,
				    PRIMARY KEY (parent, id)
				);
		""");

		connection.createStatement().execute("""
				CREATE INDEX index_aj ON adjacent (id);
		""");

		var stat = connection.prepareStatement("INSERT INTO category (id, name) VALUES (?,?)");
		var stat2 = connection.prepareStatement("INSERT INTO category_tree (ancestor, descendant, distance) VALUES (?,?,?)");
		var stat3 = connection.prepareStatement("INSERT INTO adjacent (id, parent, name) VALUES (?,?,?)");

		var loader = Benchmark.class.getClassLoader();
		var stream = loader.getResourceAsStream("area_code_2022.csv");

		try (var reader = new BufferedReader(new InputStreamReader(stream))) {
			String line;
			while ((line = reader.readLine()) != null) {
				var columns = line.split(",");
				var code = Long.parseLong(columns[0]);

				stat.setLong(1, code);
				stat.setString(2, columns[1]);
				stat.execute();

				stat3.setLong(1, code);
				stat3.setLong(2, Long.parseLong(columns[3]));
				stat3.setString(3, columns[1]);
				stat3.execute();

				var level = Integer.parseInt(columns[2]);
				for (int i = 0; i < level; i++) {
					var anc = code / MASKS[5 - level + i] * MASKS[5 - level + i];
					stat2.setLong(1, anc);
					stat2.setLong(2, code);
					stat2.setInt(3, i);
					stat2.addBatch();
				}

				stat2.setLong(1, 0);
				stat2.setLong(2, code);
				stat2.setInt(3, level);
				stat2.addBatch();
				stat2.executeBatch();
			}
		}
	}
}
