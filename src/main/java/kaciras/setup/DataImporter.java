package kaciras.setup;

import kaciras.Utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class DataImporter implements AutoCloseable {

	private static final String SEPARATOR = "-------- 下面的部分，在导入初始数据后执行 --------";

	private final Connection connection;
	private final String postScript;

	DataImporter(Connection connection, String sqlFile) throws IOException {
		var parts = loadSchemaFile(sqlFile);
		this.connection = connection;
		this.postScript = parts[1];
		Utils.executeScript(connection, parts[0]);
	}

	/**
	 * 读取 resources 目录下的 SQL 脚本文件，自动选择对应数据库的文件夹。
	 * <p>
	 * 加载后的脚本分为建表和后处理两个部分，
	 *
	 * @param path SQL 脚本的路径
	 * @throws IOException 如果出现异常
	 */
	private String[] loadSchemaFile(String path) throws IOException {
		var loader = Utils.class.getClassLoader();

		try (var stream = loader.getResourceAsStream(path)) {
			if (stream == null) {
				throw new FileNotFoundException(path);
			}
			return new String(stream.readAllBytes(), StandardCharsets.UTF_8).split(SEPARATOR);
		}
	}

	@Override
	public void close() {
		Utils.executeScript(connection, postScript);
	}

	public abstract void importData(DataRow items) throws SQLException;

	static class Closure extends DataImporter {

		private final PreparedStatement attr;
		private final PreparedStatement tree;

		public Closure(Connection connection, String postScript) throws Exception {
			super(connection, postScript);
			attr = connection.prepareStatement("INSERT INTO category (id, name) VALUES (?,?)");
			tree = connection.prepareStatement("INSERT INTO category_tree (ancestor, descendant, distance) VALUES (?,?,?)");
		}

		@Override
		public void importData(DataRow row) throws SQLException {
			var ancestor = row.getAncestorIds();

			attr.setLong(1, row.getId());
			attr.setString(2, row.getName());
			attr.execute();

			for (int i = 0; i < ancestor.length; i++) {
				tree.setLong(1, ancestor[i]);
				tree.setLong(2, row.getId());
				tree.setInt(3, i);
				tree.addBatch();
			}

			tree.setLong(1, 0);
			tree.setLong(2, row.getId());
			tree.setInt(3, ancestor.length);
			tree.addBatch();
			tree.executeBatch();
		}
	}

	static class Adjacent extends DataImporter {

		private final PreparedStatement stat;

		public Adjacent(Connection connection, String postScript) throws Exception {
			super(connection, postScript);
			stat = connection.prepareStatement("INSERT INTO adjacent (id, parent, name) VALUES (?,?,?)");
		}

		@Override
		public void importData(DataRow row) throws SQLException {
			stat.setLong(1, row.getId());
			stat.setLong(2, row.getAncestorIds()[0]);
			stat.setString(3, row.getName());
			stat.execute();
		}
	}
}
