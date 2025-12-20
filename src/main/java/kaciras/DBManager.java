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
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 负责管理数据库及连接的类，包含创建连接，导入数据，清除数据三个功能。
 */
@RequiredArgsConstructor
@Getter
public abstract class DBManager {

	private static final String SEPARATOR = "-------- 下面的部分，在导入初始数据后执行 --------";

	private final Properties properties;
	private final DataSource dataSource;

	public static DBManager open() throws IOException {
		var properties = new Properties();
		try (var stream = Utils.loadConfig()) {
			properties.load(stream);
		}

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

	/**
	 * 读取 resources 目录下的 SQL 脚本文件，自动选择对应数据库的文件夹。
	 * <p>
	 * 加载后的脚本分为建表和后处理两个部分，
	 *
	 * @param name        SQL 脚本的文件名
	 * @throws IOException 如果出现异常
	 */
	private String[] loadSchemaFile(String name) throws IOException {
		var loader = Utils.class.getClassLoader();

		var path = this.getClass().getSimpleName();
		path = path.substring(0, path.length()-"Manager".length());
		path = path.toLowerCase() + "/" + name;

		try (var stream = loader.getResourceAsStream(path)) {
			if (stream == null) {
				throw new FileNotFoundException(path);
			}
			return new String(stream.readAllBytes(), StandardCharsets.UTF_8).split(SEPARATOR);
		}
	}

	void executeScript(Connection connection, String sqlScript) {
		var runner = new ScriptRunner(connection);
		runner.setLogWriter(null);
		runner.setEscapeProcessing(false);
		runner.runScript(new StringReader(sqlScript));
	}

	/**
	 * 运行 resources 目录下的 SQL 脚本文件。
	 *
	 * @param connection 数据库连接
	 * @param url    文件路径（ClassPath）
	 * @throws IOException 如果出现异常
	 */
	void executeScriptFile(Connection connection, String url) throws IOException {
		var loader = Utils.class.getClassLoader();
		try (var stream = loader.getResourceAsStream(url)) {
			if (stream == null) {
				throw new FileNotFoundException(url);
			}
			var runner = new ScriptRunner(connection);
			runner.setLogWriter(null);
			runner.setEscapeProcessing(false);
			runner.runScript(new InputStreamReader(stream));
		}
	}

	public final void importClosureTable() throws Exception {
		try(var connection = getDataSource().getConnection()) {
			connection.setAutoCommit(false);
			var scripts = loadSchemaFile("closure.sql");
			executeScript(connection, scripts[0]);
			executeScriptFile(connection, "data.sql");
			executeScript(connection, scripts[1]);
		}
	}

	public final void importAdjacentTable() throws Exception {
		try(var connection = getDataSource().getConnection()) {
			connection.setAutoCommit(false);
			var scripts = loadSchemaFile("adjacent.sql");
			executeScript(connection, scripts[0]);



			executeScript(connection, scripts[1]);
		}
	}

	public abstract void createTable(Connection connection) throws Exception;

	public abstract int getDBSize() throws SQLException;

	public abstract void postInitialize(Connection connection) throws Exception;



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
		public void createTable(Connection connection) throws Exception {
			super.executeScript(connection, "sqlite/closure.sql");
		}

		@Override
		public int getDBSize() throws SQLException {
			@Cleanup var connection = super.dataSource.getConnection();
			var stat = connection.createStatement();
			var resultSet = stat.executeQuery(
					"SELECT (page_count - freelist_count) * page_size " +
							"FROM pragma_page_count(), pragma_freelist_count(), pragma_page_size()");
			return resultSet.getInt(1);
		}

		@Override
		public void postInitialize(Connection connection) throws Exception {
			@Cleanup var stat = connection.createStatement();
			stat.execute("");
		}
	}

	private static final class MysqlManager extends DBManager {

		public MysqlManager(Properties properties, DataSource dataSource) {
			super(properties, dataSource);
		}

		@Override
		public void createTable(Connection connection) throws Exception {
			super.executeScript(connection, "mysql/closure.sql");
		}

		@Override
		public int getDBSize() throws SQLException {
			@Cleanup var connection = super.dataSource.getConnection();
			var stat = connection.createStatement();
			var resultSet = stat.executeQuery("SELECT SUM(data_length + index_length) FROM information_schema.tables WHERE TABLE_SCHEMA='test'");
			return resultSet.getInt(1);
		}

		@Override
		public void postInitialize(Connection connection) throws Exception {
			@Cleanup var stat = connection.createStatement();

		}
	}

	private static final class PostgresManager extends DBManager {

		public PostgresManager(Properties properties, DataSource dataSource) {
			super(properties, dataSource);
		}

		@Override
		public void createTable(Connection connection) throws Exception {
			super.executeScript(connection, "postgres/closure.sql");
		}

		@Override
		public int getDBSize() throws SQLException {
			return 0;
		}

		@Override
		public void postInitialize(Connection connection) throws Exception {
			// PG 如果指定了 id 自增记录就不会增加，需要手动修复，这一点很不人性化。
			@Cleanup var stat = connection.createStatement();
			stat.execute("SELECT setval('category_id_seq', (SELECT MAX(id) FROM category)+1)");
		}
	}
}
