package kaciras;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import lombok.AllArgsConstructor;
import org.apache.ibatis.session.SqlSession;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 一个简单的控制器绑定实现，将 HTTP 请求转为对控制器方法的调用。
 *
 * 请求使用 POST 方法，路径 为 /api/[方法名]，其中[方法名]是 Controller 类中方法的名字；
 * 参数以 JSON 数组的形式序列化，作为请求体；响应也使用 JSON。
 *
 * 例如：POST /api/update [1, "new name"]
 * 会调用 Controller.update 方法，参数分别为 1 和 "new name"。
 */
public final class Dispatcher implements UncheckedHttpHandler {

	@AllArgsConstructor
	private static final class ResultView {
		public final String[] sqls;
		public final long time;
		public final Object data;
	}

	@AllArgsConstructor
	private static final class ErrorView {
		public final String type;
		public final String message;
	}

	private final TrackingDataSource dataSource;
	private final SqlSession session;
	private final Controller controller;

	private final ObjectMapper objectMapper;
	private final Map<String, Method> methodTable;

	public Dispatcher(TrackingDataSource dataSource, SqlSession session, Controller controller) {
		this.dataSource = dataSource;
		this.session = session;
		this.controller = controller;

		objectMapper = new ObjectMapper()
				.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
				.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

		methodTable = Arrays.stream(Controller.class.getDeclaredMethods())
				.collect(Collectors.toMap(Method::getName, Function.identity()));
	}

	@Override
	public void handle(HttpExchange exchange) throws Exception {
		var endpoint = exchange.getRequestURI().getPath().substring(5);
		var method = methodTable.get(endpoint);

		if (method != null) {
			invoke(exchange, method);
		} else {
			exchange.sendResponseHeaders(404, 0);
		}
	}

	private void invoke(HttpExchange exchange, Method method) throws Exception {
		var body = objectMapper.readTree(exchange.getRequestBody());

		var params = method.getParameters();
		var args = new Object[params.length];
		for (var i = 0; i < args.length; i++) {
			var type = params[i].getType();
			var name = params[i].getName();

			var jsonValue = body.get(name);
			if (jsonValue == null) {
				throw new ReflectiveOperationException("缺少参数：" + name);
			}
			args[i] = objectMapper.treeToValue(jsonValue, type);
		}

		dataSource.reset();

		try {
			var start = System.currentTimeMillis();
			var data = method.invoke(controller, args);
			session.commit();
			var time = System.currentTimeMillis() - start;
			var sqls = dataSource.getExecutedSqls();
			respond(exchange, 200, new ResultView(sqls, time, data));
		} catch (InvocationTargetException ex) {
			var cause = ex.getCause();
			var type = cause.getClass().getSimpleName();
			var message = cause.getMessage();
			ex.printStackTrace();
			respond(exchange, 400, new ErrorView(type, message));
		}
	}

	private void respond(HttpExchange exchange, int status, Object body) throws Exception {
		var headers = exchange.getResponseHeaders();
		headers.add("Content-Type", "application/json");

		var json = objectMapper.writeValueAsBytes(body);
		exchange.sendResponseHeaders(status, json.length);
		exchange.getResponseBody().write(json);
	}
}
