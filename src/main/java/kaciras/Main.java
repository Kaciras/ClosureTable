package kaciras;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

public final class Main {

	private static final String HOST_NAME = "localhost";
	private static final int PORT = 6666;

	public static void main(String[] args) throws Exception {
		Utils.disableIllegalAccessWarning();

		var dataSource = new TrackingDataSource(Utils.getDaraSource());
		var session = Utils.createSqlSession(dataSource);
		Utils.executeScript(session.getConnection(), "schema.sql");
		Utils.executeScript(session.getConnection(), "data.sql");

		var mapper = session.getMapper(CategoryMapper.class);
		Category.categoryMapper = mapper;
		var controller = new Controller(new Repository(mapper));
		var api = new HttpAdapter(dataSource, controller);

		var server = HttpServer.create(new InetSocketAddress(HOST_NAME, PORT), 0);
		server.createContext("/api/", wrapHandler(api));
		server.createContext("/", wrapHandler(Main::serveStaticResource));
		server.start();

		// 玩完记得把表删了
		Runnable cleanup = () -> Utils.dropTables(session.getConnection());
		Runtime.getRuntime().addShutdownHook(new Thread(cleanup));

		System.out.println("Demo hosted on http://" + HOST_NAME + ":" + PORT);
	}

	/**
	 * 包装一个 HttpHandler，为其增加异常处理和自动关闭 HttpExchange 功能，
	 * 同时允许参数抛出更宽泛的异常。
	 *
	 * @param handler 被包装的 HttpHandler
	 * @return 包装后的 HttpHandler
	 */
	private static HttpHandler wrapHandler(UncheckedHttpHandler handler) {
		return (HttpExchange exchange) -> {
			try (exchange) {
				handler.handle(exchange);
			} catch (Exception e) {
				e.printStackTrace();
				exchange.sendResponseHeaders(500, 0);
			}
		};
	}

	private static void serveStaticResource(HttpExchange exchange) throws IOException {
		var name = exchange.getRequestURI().getPath();
		if ("/".equals(name)) {
			name = "index.html";
		}
		var path = Path.of("web", name);

		try {
			// 先检查下文件是否存在，JS 的扩展名容易漏。
			var size = Files.size(path);

			// probeContentType 对 .js 返回错误的 text/plain
			var mime = Files.probeContentType(path);
			if (name.endsWith(".js")) {
				mime = "application/javascript";
			}
			exchange.getResponseHeaders().add("Content-Type", mime);

			exchange.sendResponseHeaders(200, size);
			Files.copy(path, exchange.getResponseBody());
		} catch (NoSuchFileException ex) {
			exchange.sendResponseHeaders(404, 0);
		}
	}
}
