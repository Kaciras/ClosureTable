package kaciras;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import org.apache.ibatis.reflection.ParamNameResolver;
import org.apache.ibatis.session.Configuration;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class HttpAPI implements UncheckedHttpHandler {

	private static final class ResultView {
		public String sql;
		public long time;
		public Object data;
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
		for (int i = 0; i < args.length; i++) {
			args[i] = objectMapper.treeToValue(body.get(i), params[i].getType());
		}

		var result = new ResultView();
		var start = System.currentTimeMillis();
		result.data = method.invoke(sqlMapper, args);
		result.time = System.currentTimeMillis() - start;
		result.sql = getSql(method, args);

		var json = objectMapper.writeValueAsBytes(result);
		exchange.getResponseHeaders().add("Content-Type", "application/json");
		exchange.sendResponseHeaders(200, json.length);
		exchange.getResponseBody().write(json);
	}

	private String getSql(Method method, Object[] args) {
		var pns = new ParamNameResolver(config, method);
		var boundSql = config.getMappedStatement(method.getName()).getBoundSql(pns.getNamedParams(args));
		var template = boundSql.getSql().replace("?", "%s");
		var s = boundSql.getParameterMappings().stream().map(p -> p.getExpression()).toArray();
		return String.format(template, s);
	}
}
