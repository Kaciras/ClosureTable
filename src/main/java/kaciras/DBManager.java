package kaciras;

import lombok.Cleanup;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.jdbc.ScriptRunner;

import javax.sql.DataSource;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 负责管理数据库及连接的类，包含创建连接，导入数据，清除数据三个功能。
 */
@RequiredArgsConstructor
@Getter
public abstract class DBManager {

	private final Properties properties;
	private final DataSource dataSource;

	public static DBManager open() throws IOException {
		var properties = new Properties();
		@Cleanup var stream = Utils.loadConfig();
		properties.load(stream);

		var dataSource = new PooledDataSource(
				properties.getProperty("DRIVER"),
				properties.getProperty("URL"),
				properties.getProperty("USER"),
				properties.getProperty("PASSWORD")
		);

		var driver = properties.getProperty("URL").split(":")[1];
		return switch (driver) {
			case "mariadb" -> new MysqlManager(properties, dataSource);
			case "sqlite" -> new SqliteManager(properties, dataSource);
			case "postgresql" -> new PostgresManager(properties, dataSource);
			default -> throw new IOException(driver + " is not supported");
		};
	}

	@SuppressWarnings("SqlResolve")
	void importDemoData() throws Exception {
		this.importData((r, c) -> executeScript(r, "data.sql"));
	}

	void importData(SQLOperation operation) throws Exception {
		@Cleanup var connection = dataSource.getConnection();

		var runner = new ScriptRunner(connection);
		runner.setLogWriter(null);
		runner.setEscapeProcessing(false);

		connection.setAutoCommit(false);
		this.importData(runner, connection, operation);
		connection.commit();
	}

	public abstract void importData(
			ScriptRunner runner,
			Connection connection,
			SQLOperation operation) throws Exception;

	abstract int getDBSize() throws SQLException;

	/**
	 * 运行资源目录下的 SQL 脚本文件。
	 *
	 * @param runner 运行器
	 * @param url    文件路径（ClassPath）
	 * @throws IOException 如果出现异常
	 */
	private void executeScript(ScriptRunner runner, String url) throws IOException {
		var loader = Utils.class.getClassLoader();
		var stream = loader.getResourceAsStream(url);
		if (stream == null) {
			throw new FileNotFoundException(url);
		}
		@Cleanup var reader = new InputStreamReader(stream);
		runner.runScript(reader);
	}

	/**
	 * 删除 category 和 category_tree 两张表。
	 */
	@SneakyThrows
	public void dropTables() {
		@Cleanup var connection = dataSource.getConnection();
		@Cleanup var statement = connection.createStatement();

		statement.execute("DROP TABLE category");
		statement.execute("DROP TABLE category_tree");
	}

	private static final class SqliteManager extends DBManager {

		public SqliteManager(Properties properties, DataSource dataSource) {
			super(properties, dataSource);
		}

		@Override
		public void importData(ScriptRunner runner, Connection connection, SQLOperation operation) throws Exception {
			super.executeScript(runner, "schema-sqlite.sql");
			operation.run(runner, connection);
		}

		@Override
		int getDBSize() throws SQLException {
			@Cleanup var connection = super.dataSource.getConnection();
			var stat = connection.createStatement();
			var resultSet = stat.executeQuery(
					"SELECT (page_count - freelist_count) * page_size " +
					"FROM pragma_page_count(), pragma_freelist_count(), pragma_page_size()");
			return resultSet.getInt(1);
		}
	}

	private static final class MysqlManager extends DBManager {

		public MysqlManager(Properties properties, DataSource dataSource) {
			super(properties, dataSource);
		}

		@Override
		public void importData(ScriptRunner runner, Connection connection, SQLOperation operation) throws Exception {
			super.executeScript(runner, "schema-mysql.sql");
			operation.run(runner, connection);

			@Cleanup var stat = connection.createStatement();
			stat.execute("ALTER TABLE category_tree ADD PRIMARY KEY (descendant, distance, ancestor)");
			stat.execute("CREATE INDEX index_0 ON category_tree (ancestor, distance)");
		}

		@Override
		int getDBSize() throws SQLException {
			@Cleanup var connection = super.dataSource.getConnection();
			var stat = connection.createStatement();
			var resultSet = stat.executeQuery("SELECT SUM(data_length + index_length) FROM information_schema.tables WHERE TABLE_SCHEMA='test'");
			return resultSet.getInt(1);
		}
	}

	private static final class PostgresManager extends DBManager {

		public PostgresManager(Properties properties, DataSource dataSource) {
			super(properties, dataSource);
		}

		@Override
		public void importData(ScriptRunner runner, Connection connection, SQLOperation operation) throws Exception {
			super.executeScript(runner, "schema-pg.sql");
			operation.run(runner, connection);

			// PG 如果指定了 id 自增记录就不会增加，需要手动修复，这一点很不人性化。
			@Cleanup var stat = connection.createStatement();
			stat.execute("SELECT setval('category_id_seq', (SELECT MAX(id) FROM category)+1)");
		}

		@Override
		int getDBSize() throws SQLException {
			return 0;
		}
	}
}
