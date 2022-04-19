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
import java.sql.SQLException;
import java.util.Properties;

/**
 * 负责管理数据库及连接的类，包含创建连接，导入数据，清除数据三个功能。
 */
@RequiredArgsConstructor
@Getter
public final class DBManager {

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

		return new DBManager(properties, dataSource);
	}

	@SuppressWarnings("SqlResolve")
	void importData() throws IOException, SQLException {
		@Cleanup var connection = dataSource.getConnection();

		var runner = new ScriptRunner(connection);
		runner.setLogWriter(null);
		runner.setEscapeProcessing(false);

		var driver = properties.getProperty("URL").split(":")[1];
		if (!driver.equals("postgresql")) {
			var script = driver.equals("sqlite") ? "schema-sqlite.sql" : "schema-mysql.sql";
			executeScript(runner, script);
			executeScript(runner, "data.sql");
		} else {
			executeScript(runner, "schema-pg.sql");
			executeScript(runner, "data.sql");

			// PG 如果指定了 id 自增记录就不会增加，需要手动修复，这一点很不人性化。
			@Cleanup var stat = connection.createStatement();
			stat.execute("SELECT setval('category_id_seq', (SELECT MAX(id) FROM category)+1)");
		}
	}

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
}
