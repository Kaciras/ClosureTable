package kaciras;

import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * 为了拿到 SQL 语句，需要拦截 Statement.execute() 方法，
 * 根据调用链，得做 DataSource -> Connection -> Statement 三层包装，真他妈麻烦。
 * <p>
 * 该类是第一层，包装 DataSource 拦截 getConnection()
 */
@RequiredArgsConstructor
public final class TrackingDataSource implements DataSource {

	// 因为演示中不存在并发，就没有用加锁的 List。
	private final List<String> records = new ArrayList<>();

	private final DataSource dataSource;

	public void reset() {
		records.clear();
	}

	/**
	 * 获取所有执行过的 SQL 语句，参数已经填充好了。
	 */
	public String[] getExecutedSql() {
		return records.toArray(String[]::new);
	}

	/**
	 * 创建代理的参数很长，单独提取成一个方法减少点字数。
	 */
	@SuppressWarnings("unchecked")
	private <T> T createProxy(Class<T> clazz, InvocationHandler handler) {
		var loader = TrackingDataSource.class.getClassLoader();
		return (T) Proxy.newProxyInstance(loader, new Class[]{clazz}, handler);
	}

	@Override
	public Connection getConnection() throws SQLException {
		return createProxy(Connection.class, new ConnectionHandler(dataSource.getConnection()));
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		var inner = dataSource.getConnection(username, password);
		return createProxy(Connection.class, new ConnectionHandler(inner));
	}

	/**
	 * 第二层，代理 Connection 对象，拦截 prepareStatement() 方法，返回代理后的 Statement。
	 *
	 * @see ArgRecordHandler
	 */
	@RequiredArgsConstructor
	private final class ConnectionHandler implements InvocationHandler {

		private final Connection connection;

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			var returnValue = method.invoke(connection, args);

			if (!method.getName().equals("prepareStatement")) {
				return returnValue;
			}

			var stat = (PreparedStatement) returnValue;
			var recorder = new ArgRecordHandler(stat, (String) args[0], records);
			return createProxy(PreparedStatement.class, recorder);
		}
	}

	// ======================== 下面都是直接转发 ========================

	@Override
	public PrintWriter getLogWriter() throws SQLException {
		return dataSource.getLogWriter();
	}

	@Override
	public void setLogWriter(PrintWriter out) throws SQLException {
		dataSource.setLogWriter(out);
	}

	@Override
	public void setLoginTimeout(int seconds) throws SQLException {
		dataSource.setLoginTimeout(seconds);
	}

	@Override
	public int getLoginTimeout() throws SQLException {
		return dataSource.getLoginTimeout();
	}

	@Override
	public Logger getParentLogger() throws SQLFeatureNotSupportedException {
		return dataSource.getParentLogger();
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return dataSource.unwrap(iface);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return dataSource.isWrapperFor(iface);
	}
}
