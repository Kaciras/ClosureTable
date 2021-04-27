package kaciras;

import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import javax.sql.DataSource;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public final class Utils {

	private Utils() {}

	/**
	 * 用于检查Update，Delete等SQL语句是否产生了影响，没产生影响时将抛出异常
	 *
	 * @param rows 影响行数
	 * @throws IllegalArgumentException 如果没有影响任何行
	 */
	public static void checkEffective(int rows) {
		if (rows <= 0) throw new IllegalArgumentException();
	}

	public static void checkPositive(int value, String valname) {
		if (value <= 0) throw new IllegalArgumentException("参数" + valname + "必须是正数:" + value);
	}

	public static void checkNotNegative(int value, String valname) {
		if (value < 0) throw new IllegalArgumentException("参数" + valname + "不能为负:" + value);
	}

	public static SqlSession createSqlSession() throws IOException {
		var configFile = Path.of("application.local.properties");
		if (!Files.exists(configFile)) {
			configFile = Path.of("application.properties");
		}

		var props = new Properties();
		try (var stream = Files.newInputStream(configFile)) {
			props.load(stream);
		}

		return Utils.createSqlSession(props.getProperty("DRIVER"), props.getProperty("URL"),
				props.getProperty("USER"), props.getProperty("PASSWORD"));
	}

	public static SqlSession createSqlSession(String driver, String url, String user, String password) {
		var dataSource = new UnpooledDataSource();
		dataSource.setDriver(driver);
		dataSource.setUrl(url);
		dataSource.setUsername(user);
		dataSource.setPassword(password);
		dataSource.setAutoCommit(false);
		return createSqlSession(new PooledDataSource(dataSource));
	}

	public static SqlSession createSqlSession(DataSource dataSource) {
		var txFactory = new JdbcTransactionFactory();
		var environment = new Environment("test", txFactory, dataSource);

		var config = new Configuration();
		config.setCacheEnabled(false);
		config.addMapper(CategoryMapper.class);
		config.setEnvironment(environment);

		return new DefaultSqlSessionFactory(config).openSession();
	}

	/**
	 * 运行资源目录下的 SQL 脚本文件。
	 *
	 * @param connection 数据库连接
	 * @param url        文件路径（ClassPath）
	 * @throws Exception 如果出现异常
	 */
	public static void executeScript(Connection connection, String url) throws Exception {
		var stream = Utils.class.getClassLoader().getResourceAsStream(url);
		if (stream == null) {
			throw new FileNotFoundException(url);
		}

		var runner = new ScriptRunner(connection);
		runner.setLogWriter(null);

		try (var r = new InputStreamReader(stream)) {
			runner.runScript(r);
			connection.commit();
		}
	}

	public static void dropTables(Connection connection) {
		try {
			var statement = connection.createStatement();
			statement.execute("DROP TABLE category");
			statement.execute("DROP TABLE category_tree");
			statement.close();
			connection.commit();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public static void disableIllegalAccessWarning() {
		var javaVersionElements = System.getProperty("java.version").split("\\.");
		if (Integer.parseInt(javaVersionElements[0]) == 1) {
			return; // 1.8.x_xx or lower
		}
		try {
			var theUnsafe = Class.forName("sun.misc.Unsafe").getDeclaredField("theUnsafe");
			theUnsafe.setAccessible(true);
			var u = theUnsafe.get(null);

			var cls = Class.forName("jdk.internal.module.IllegalAccessLogger");
			var logger = cls.getDeclaredField("logger");

			var offset = (long) u.getClass()
					.getMethod("staticFieldOffset", Field.class).invoke(u, logger);

			u.getClass().getMethod("putObjectVolatile", Object.class, long.class, Object.class)
					.invoke(u, cls, offset, null);
		} catch (Exception ignore) {
			throw new UnsupportedClassVersionError("Can not desable illegal access warning");
		}
	}
}
