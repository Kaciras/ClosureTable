package net.kaciras.example;

import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import javax.sql.DataSource;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;

final class Utils {

	/**
	 * 用于检查Update，Delete等SQL语句是否产生了影响，没产生影响时将抛出异常
	 *
	 * @param rows 影响行数
	 * @throws IllegalArgumentException 如果没有影响任何行
	 */
	public static void checkEffective(int rows) {
		if(rows <= 0) throw new IllegalArgumentException();
	}

	public static void checkPositive(int value, String valname) {
		if (value <= 0) throw new IllegalArgumentException("参数" + valname + "必须是正数:" + value);
	}

	public static void checkNotNegative(int value, String valname) {
		if (value < 0) throw new IllegalArgumentException("参数" + valname + "不能为负:" + value);
	}

	public static <T> T notNull(T obj) {
		if(obj == null) {
			throw new IllegalArgumentException("指定的分类不存在");
		}
		return obj;
	}

	public static SqlSession createSqlSession(String driver, String url, String user, String password) {
		UnpooledDataSource dataSource = new UnpooledDataSource();
		dataSource.setDriver(driver);
		dataSource.setUrl(url);
		dataSource.setUsername(user);
		dataSource.setPassword(password);
		dataSource.setAutoCommit(false);
		return createSqlSession(dataSource);
	}

	public static SqlSession createSqlSession(DataSource dataSource) {
		TransactionFactory transactionFactory = new JdbcTransactionFactory();
		Environment environment = new Environment("test", transactionFactory, dataSource);

		Configuration config = new Configuration();
		config.addMapper(CategoryMapper.class);
		config.setEnvironment(environment);

		SqlSessionFactory sessionFactory = new DefaultSqlSessionFactory(config);
		return sessionFactory.openSession();
	}

	/**
	 * 运行SQL脚本文件
	 *
	 * @param connection 数据库连接
	 * @param url 文件路径（ClassPath）
	 * @throws Exception 如果出现异常
	 */
	public static void executeScript(Connection connection, String url) throws Exception {
		InputStream stream = Utils.class.getClassLoader().getResourceAsStream(url);
		try(Reader r = new InputStreamReader(stream)) {
			ScriptRunner scriptRunner = new ScriptRunner(connection);
			scriptRunner.setLogWriter(null);
			scriptRunner.runScript(r);
			connection.commit();
			scriptRunner.closeConnection();
		}
	}

	private Utils() {}
}
