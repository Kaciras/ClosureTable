package kaciras;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import lombok.AllArgsConstructor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 一个简单的 HTTP 控制器绑定实现。
 */
public final class HttpAdapter implements UncheckedHttpHandler {

	@AllArgsConstructor
	private static final class ResultView {
		public final String[] sql;
		public final long time;
		public final Object data;
	}

	@AllArgsConstructor
	private static final class ErrorView {
		public final String type;
		public final String message;
	}

	private final ObjectMapper objectMapper;

	private final TrackingDataSource dataSource;
	private final Controller controller;
	private final Map<String, Method> methodTable;

	public HttpAdapter(TrackingDataSource dataSource, Controller controller) {
		this.dataSource = dataSource;
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
			var time = System.currentTimeMillis() - start;
			var sql = dataSource.getExecutedSql();
			respond(exchange, 200, new ResultView(sql, time, data));
		} catch (ReflectiveOperationException ex) {
			throw ex;
		} catch (Exception ex) {
			var type = ex.getClass().getSimpleName();
			var message = ex.getMessage();
			respond(exchange, 400, new ErrorView(type, message));
		}
	}

	private void respond(HttpExchange exchange, int status, Object body) throws Exception {
		var headers = exchange.getResponseHeaders();
		var json = objectMapper.writeValueAsBytes(body);

		headers.add("Content-Type", "application/json");
		exchange.sendResponseHeaders(status, json.length);
		exchange.getResponseBody().write(json);
	}
}
