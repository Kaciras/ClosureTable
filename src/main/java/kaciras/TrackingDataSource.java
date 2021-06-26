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
 * 通过代理记录创建的数据库连接，接着用同样的方法记录所有创建的 Statement 对象。
 * 最后用 Statement 的参数还原执行的 SQL 以显示在演示页面里。
 *
 * <h3>吐槽</h3>
 * 就为了获取 SQL 搞了个三层代理，DataSource -> Connection -> Statement，真他妈麻烦。
 */
@RequiredArgsConstructor
public final class TrackingDataSource implements DataSource {

	private final List<ArgRecordHandler> records = new ArrayList<>();

	private final DataSource dataSource;

	public void reset() {
		records.clear();
	}

	/**
	 * 获取所有记录的 SQL 语句，已对参数化查询进行处理，结果接近真实的 SQL。
	 *
	 * @return SQL 语句数组
	 */
	public String[] getExecutedSqls() {
		return records.stream().map(ArgRecordHandler::getExecutedSql).toArray(String[]::new);
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
		return createProxy(Connection.class, new TrackHandler(dataSource.getConnection()));
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		var inner = dataSource.getConnection(username, password);
		return createProxy(Connection.class, new TrackHandler(inner));
	}

	@RequiredArgsConstructor
	private final class TrackHandler implements InvocationHandler {

		private final Connection connection;

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			var returnValue = method.invoke(connection, args);

			if (!method.getName().equals("prepareStatement")) {
				return returnValue;
			}

			var stat = (PreparedStatement) returnValue;
			var recorder = new ArgRecordHandler(stat, (String) args[0]);
			records.add(recorder);
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
