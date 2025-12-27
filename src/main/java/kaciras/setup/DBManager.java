package kaciras.setup;

import kaciras.Utils;
import lombok.Cleanup;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

/**
 * 负责管理数据库及连接的类，包含创建连接，导入数据，清除数据三个功能。
 */
@RequiredArgsConstructor
@Getter
public final class DBManager {

	private static final String SEPARATOR = "-------- 下面的部分，在导入初始数据后执行 --------";

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

	/**
	 * 读取 resources 目录下的 SQL 脚本文件，自动选择对应数据库的文件夹。
	 * <p>
	 * 加载后的脚本分为建表和后处理两个部分，
	 *
	 * @param name SQL 脚本的文件名
	 * @throws IOException 如果出现异常
	 */
	private String[] loadSchemaFile(String name) throws IOException {
		var loader = Utils.class.getClassLoader();
		var path = dialect + "/" + name;

		try (var stream = loader.getResourceAsStream(path)) {
			if (stream == null) {
				throw new FileNotFoundException(path);
			}
			return new String(stream.readAllBytes(), StandardCharsets.UTF_8).split(SEPARATOR);
		}
	}

	public DataImporter createTable(String sqlName) throws Exception {
		var scripts = loadSchemaFile(sqlName);
		Utils.executeScript(connection, scripts[0]);

		if (sqlName.equals("adjacent.sql")) {
			return new DataImporter.Adjacent(connection, scripts[1]);
		} else {
			return new DataImporter.Closure(connection, scripts[1]);
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
