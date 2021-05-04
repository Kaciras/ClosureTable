package kaciras;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.reflection.ParamNameResolver;
import org.apache.ibatis.session.Configuration;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;
import java.util.IdentityHashMap;
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

	private final Map<Class<? extends Annotation>, Function<Annotation, String[]>> sqlProviders = Map.of(
			Insert.class, a -> ((Insert) a).value(),
			Delete.class, a -> ((Delete) a).value(),
			Update.class, a -> ((Update) a).value(),
			Select.class, a -> ((Select) a).value()
	);

	private final ObjectMapper objectMapper = new ObjectMapper();

	private final Configuration config;
	private final CategoryMapper sqlMapper;

	private final Map<Method, String[]> sqlTable = new IdentityHashMap<>();
	private final Map<String, Method> methodTable;

	public HttpAPI(Configuration config, CategoryMapper sqlMapper) {
		this.config = config;
		this.sqlMapper = sqlMapper;

		methodTable = Arrays.stream(CategoryMapper.class.getMethods())
				.peek(this::collectSql)
				.collect(Collectors.toMap(Method::getName, Function.identity()));
	}

	private void collectSql(Method method) {
		for (var e : sqlProviders.entrySet()) {
			var annotation = method.getDeclaredAnnotation(e.getKey());
			if (annotation != null) {
				sqlTable.put(method, e.getValue().apply(annotation));
				return;
			}
		}
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
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

		exchange.close();
	}

	@SuppressWarnings("unchecked")
	private static Map<String, String> getParams(URI uri) {
		var segments = uri.getQuery().split("&");
		var entries = Arrays.stream(segments)
				.map(s -> s.split("=", 2))
				.map(s -> Map.entry(s[0], s[1]));
		return Map.ofEntries(entries.toArray(Map.Entry[]::new));
	}
}
