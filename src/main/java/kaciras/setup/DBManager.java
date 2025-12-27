package kaciras.setup;

import kaciras.Utils;
import lombok.Cleanup;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * 负责管理数据库及连接的类，包含创建连接，导入数据，清除数据三个功能。
 */
@RequiredArgsConstructor
@Getter
public final class DBManager {

	private final String dialect;
	private final Properties properties;

	/**
	 * 本项目仅需要单个连接，而且内存数据库也必须这样做，所以直接在此保持一个，无需关闭。
	 */
	private final Connection connection;

	public static DBManager open() throws Exception {
		var properties = new Properties();
		try (var stream = Utils.loadConfig()) {
			properties.load(stream);
		}

		var connection = DriverManager.getConnection(
				properties.getProperty("URL"),
				properties.getProperty("USER"),
				properties.getProperty("PASSWORD")
		);

		var driver = properties.getProperty("URL").split(":")[1];
		var dialect = switch (driver) {
			case "mysql", "mariadb" -> "mysql";
			case "sqlite" -> "sqlite";
			case "postgresql" -> "postgres";
			default -> throw new IOException(driver + " is not supported");
		};

		return new DBManager(dialect, properties, connection);
	}

	public DataImporter createTable(String sqlName) throws Exception {
		var path = dialect + "/" + sqlName;
		if (sqlName.equals("adjacent.sql")) {
			return new DataImporter.Adjacent(connection, path);
		} else {
			return new DataImporter.Closure(connection, path);
		}
	}

	public boolean tableExists(String table) {
		try (var statement = connection.createStatement()) {
			statement.execute("SELECT 1 FROM " + table);
			return true;
		} catch (SQLException e) {
			return false;
		}
	}

	@SneakyThrows
	public void dropTables() {
		@Cleanup var statement = connection.createStatement();

		statement.execute("DROP TABLE IF EXISTS category");
		statement.execute("DROP TABLE IF EXISTS category_tree");
		statement.execute("DROP TABLE IF EXISTS adjacent");
	}
}
