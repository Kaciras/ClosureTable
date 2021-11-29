package kaciras;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.LocalCacheScope;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

final class Utils {

	/**
	 * 寻找运行目录下的配置文件，根据以下规则：
	 * 1）如果设置了 CONFIG_FILE 环境变量则读取其指定的文件。
	 * 2）尝试读取 application.local.properties。
	 * 3）如果上面两个都不存在则读取 application.properties。
	 *
	 * @return 配置信息文件流
	 * @throws IOException 如果读取文件失败
	 */
	static InputStream loadConfig() throws IOException {
		var file = Path.of("application.local.properties");
		var env = System.getenv("CONFIG_FILE");
		if (env != null) {
			file = Path.of(env);
		}
		if (!Files.exists(file)) {
			file = Path.of("application.properties");
		}
		return Files.newInputStream(file);
	}

	static SqlSession createSqlSession(DataSource dataSource) {
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

	/**
	 * 用于检查 Update，Delete 等 SQL 语句是否产生了影响，没产生影响时将抛出异常。
	 *
	 * @param rows 影响行数
	 * @throws IllegalArgumentException 如果没有影响任何行
	 */
	static void checkEffective(int rows) {
		if (rows <= 0) throw new IllegalArgumentException();
	}

	static void checkPositive(int value, String name) {
		if (value <= 0) throw new IllegalArgumentException("参数" + name + "必须是正数:" + value);
	}

	static void checkNotNegative(int value, String name) {
		if (value < 0) throw new IllegalArgumentException("参数" + name + "不能为负:" + value);
	}
}
