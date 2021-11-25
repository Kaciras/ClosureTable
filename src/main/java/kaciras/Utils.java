package kaciras;

import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.jdbc.RuntimeSqlException;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.LocalCacheScope;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import javax.sql.DataSource;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

final class Utils {

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

	/**
	 * 读取运行目录下的配置文件，创建数据源。
	 * 优先尝试 application.local.properties，没有则使用 application.properties
	 *
	 * @return Mybatis 的数据源
	 * @throws IOException 如果读取文件失败
	 */
	public static PooledDataSource getDaraSource() throws IOException {
		var configFile = Path.of("application.local.properties");
		if (!Files.exists(configFile)) {
			configFile = Path.of("application.properties");
		}

		var props = new Properties();
		try (var stream = Files.newInputStream(configFile)) {
			props.load(stream);
		}

		var dataSource = new UnpooledDataSource(
				props.getProperty("DRIVER"), props.getProperty("URL"),
				props.getProperty("USER"), props.getProperty("PASSWORD")
		);
		return new PooledDataSource(dataSource);
	}

	public static SqlSession createSqlSession(DataSource dataSource) {
		var txFactory = new JdbcTransactionFactory();
		var environment = new Environment("test", txFactory, dataSource);

		var config = new Configuration();
		config.setCacheEnabled(false); // 为了追踪执行的 SQL 必须关闭缓存。
		config.addMapper(CategoryMapper.class);
		config.setEnvironment(environment);

		/*
		 * 禁用会话级缓存，让每次执行都查询数据库，解决无法获取 Statement 的问题。
		 *
		 * 【跟 Spring 的区别】
		 * mybatis-spring 不需要这么设置，因为它有个自定义的 SqlSessionTemplate 代理了 Session 对象，
		 * 在每次执行时都重新创建 Session。
		 */
		config.setLocalCacheScope(LocalCacheScope.STATEMENT);

		return new DefaultSqlSessionFactory(config).openSession();
	}

	static void importData(PooledDataSource source, SqlSession session) throws Exception {
		var connection = session.getConnection();
		var runner = new ScriptRunner(connection);
		runner.setLogWriter(null);

		var driver = source.getUrl().split(":")[1];
		if (!driver.equals("postgresql")) {
			executeScript(runner, "schema-mysql.sql");
			executeScript(runner, "data.sql");
		} else {
			executeScript(runner, "schema-pg.sql");
			executeScript(runner, "data.sql");

			// 插入数据时如果指定了 id，PG 的自增记录不会增加，这一点很不人性化。
			try (var stat = connection.createStatement()) {
				stat.execute("SELECT setval('category_id_seq', (SELECT MAX(id) FROM category)+1)");
			}
		}
	}

	/**
	 * 运行资源目录下的 SQL 脚本文件。
	 *
	 * @param connection 数据库连接
	 * @param url        文件路径（ClassPath）
	 * @throws Exception 如果出现异常
	 */
	public static void executeScript(ScriptRunner runner, String url) throws Exception {
		var stream = Utils.class.getClassLoader().getResourceAsStream(url);
		if (stream == null) {
			throw new FileNotFoundException(url);
		}
		try (var reader = new InputStreamReader(stream)) {
			runner.runScript(reader);
		}
	}

	/**
	 * 删除 category 和 category_tree 两张表。
	 */
	public static void dropTables(Connection connection) {
		try (var statement = connection.createStatement()) {
			statement.execute("DROP TABLE category");
			statement.execute("DROP TABLE category_tree");
			connection.commit();
		} catch (SQLException ex) {
			throw new RuntimeSqlException(ex);
		}
	}
}
