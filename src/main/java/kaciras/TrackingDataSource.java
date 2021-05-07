package kaciras;

import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@RequiredArgsConstructor
public final class TrackingDataSource implements DataSource {

	private final List<Statement> statements = new ArrayList<>();

	private final DataSource dataSource;

	public void reset() {
		statements.clear();
	}

	public String[] getExecutedSql(){
		return statements.stream().map(Object::toString).toArray(String[]::new);
	}

	@Override
	public Connection getConnection() throws SQLException {
		return (Connection) Proxy.newProxyInstance(
				TrackingDataSource.class.getClassLoader(),
				new Class[]{Connection.class},
				new TrackHandler(dataSource.getConnection())
		);
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		return (Connection) Proxy.newProxyInstance(
				TrackingDataSource.class.getClassLoader(),
				new Class[]{Connection.class},
				new TrackHandler(dataSource.getConnection(username, password))
		);
	}

	@RequiredArgsConstructor
	private final class TrackHandler implements InvocationHandler {

		private final Connection connection;

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			var returnValue = method.invoke(connection, args);

			switch (method.getName()) {
				case "prepareStatement":
				case "createStatement":
					statements.add((Statement) returnValue);
			}

			return returnValue;
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
