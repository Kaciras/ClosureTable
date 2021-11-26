package kaciras;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.util.List;

/**
 * 跟踪 PreparedStatement 设置的参数，以及执行 execute* 方法时记录 SQL。
 */
final class ArgRecordHandler implements InvocationHandler {

	// 目前的 SQL 里最多只有 3 个参数。
	private final Object[] parameters = new Object[3];

	private final PreparedStatement statement;
	private final String template;
	private final List<String> records;

	ArgRecordHandler(PreparedStatement statement, String sql, List<String> records) {
		this.statement = statement;
		this.records = records;
		this.template = sql.replace("?", "%s");
	}

	@SuppressWarnings("EnhancedSwitchMigration")
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (method.getName().startsWith("execute")) {
			records.add(String.format(template, parameters));
		} else switch (method.getName()) {
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
				parameters[(int) args[0] - 1] = args[1];
				break;
			case "setString":
				parameters[(int) args[0] - 1] = "'" + args[1] + "'";
				break;
		}
		return method.invoke(statement, args);
	}
}
