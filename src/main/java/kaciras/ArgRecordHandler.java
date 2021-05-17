package kaciras;

import lombok.RequiredArgsConstructor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;

@RequiredArgsConstructor
final class ArgRecordHandler implements InvocationHandler {

	// 目前的 SQL 里最多只有 3 个参数。
	private final Object[] parameters = new Object[3];

	private final PreparedStatement statement;
	private final String sql;

	public String sqlToString() {
		return String.format(sql.replace("?", "%s"), parameters);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		switch (method.getName()) {
			case "setByte":
			case "setShort":
			case "setInt":
			case "setLong":
			case "setTime":
			case "setDate":
			case "setTimestamp":
			case "setFloat":
			case "setDouble":
			case "setBoolean":
				parameters[(Integer) args[0] - 1] = args[1];
				break;
			case "setString":
				parameters[(Integer) args[0] - 1] = "'" + args[1] + "'";
				break;
		}
		return method.invoke(statement, args);
	}
}
