package kaciras;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class HttpAdapter implements UncheckedHttpHandler {

	private static final class ResultView {
		public String[] sql;
		public long time;
		public Object data;
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
			invokeSql(exchange, method);
		} else {
			exchange.sendResponseHeaders(404, 0);
		}
	}

	private void invokeSql(HttpExchange exchange, Method method) throws Exception {
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

		var result = new ResultView();
		dataSource.reset();

		var start = System.currentTimeMillis();
		result.data = method.invoke(controller, args);
		result.time = System.currentTimeMillis() - start;
		result.sql = dataSource.getExecutedSql();

		var json = objectMapper.writeValueAsBytes(result);
		exchange.getResponseHeaders().add("Content-Type", "application/json");
		exchange.sendResponseHeaders(200, json.length);
		exchange.getResponseBody().write(json);
	}
}
