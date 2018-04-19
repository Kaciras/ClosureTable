package net.kaciras.example;

import org.apache.ibatis.jdbc.ScriptRunner;

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
