package kaciras;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.reflection.ParamNameResolver;
import org.apache.ibatis.session.Configuration;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class HttpAPI implements HttpHandler {

	@RequiredArgsConstructor
	private static final class ResultView {
		public final String sql;
		public final long time;
		public final Object data;
	}

	private final ObjectMapper objectMapper = new ObjectMapper();

	private final Configuration config;
	private final CategoryMapper sqlMapper;

	private final Map<String, Method> methodTable;

	public HttpAPI(Configuration config, CategoryMapper sqlMapper) {
		this.config = config;
		this.sqlMapper = sqlMapper;

		methodTable = Arrays.stream(CategoryMapper.class.getMethods())
				.collect(Collectors.toMap(Method::getName, Function.identity()));
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		var endpoint = exchange.getRequestURI().getPath();

		if (endpoint.startsWith("/api")) {
			invokeSQL(exchange);
		} else {
			sendFile(exchange, endpoint);
		}

		exchange.close();
	}

	private void sendFile(HttpExchange exchange, String name) throws IOException {
		if ("/".equals(name)) {
			name = "index.html";
		}
		var path = Path.of("web", name);

		// probeContentType 对 .js 返回 text/plain？
		var mime = Files.probeContentType(path);
		if (name.endsWith(".js")) {
			mime = "application/javascript";
		}
		exchange.getResponseHeaders().add("Content-Type", mime);

		exchange.sendResponseHeaders(200, Files.size(path));
		Files.copy(path, exchange.getResponseBody());
	}

	private void invokeSQL(HttpExchange exchange) throws IOException {
		var endpoint = exchange.getRequestURI().getPath().substring(1);
		var method = methodTable.get(endpoint);

		if (method == null) {
			exchange.sendResponseHeaders(404, 0);
			exchange.close();
			return;
		}
		var body = objectMapper.readTree(exchange.getRequestBody());

		var params = method.getParameters();
		var args = new Object[params.length];

		for (int i = 0; i < args.length; i++) {
			args[i] = objectMapper.treeToValue(body.get(i), params[i].getType());
		}

		var pns = new ParamNameResolver(config, method);
		var boundSql = config.getMappedStatement(endpoint).getBoundSql(pns.getNamedParams(args));

		try {
			var start = System.currentTimeMillis();
			var data = method.invoke(sqlMapper, args);
			var time = System.currentTimeMillis() - start;
			var sql = "TODO";

			var json = objectMapper.writeValueAsBytes(new ResultView(sql, time, data));
			exchange.sendResponseHeaders(200, json.length);
			exchange.getResponseBody().write(json);
		} catch (ReflectiveOperationException e) {
			e.printStackTrace();
			exchange.sendResponseHeaders(500, 0);
		}
	}
}
