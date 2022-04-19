package kaciras;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.SimpleFileServer;

import java.net.InetSocketAddress;
import java.nio.file.Path;

public final class Main {

	private static final String HOST_NAME = "localhost";
	private static final int PORT = 6666;

	/**
	 * 包装一个 HttpHandler，增加异常处理和自动关闭 HttpExchange 功能，
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

	public static void main(String[] args) throws Exception {
		System.setProperty("file.encoding", "UTF-8");

		var manager = DBManager.open();
		manager.importData();

		// 玩完记得把表删了
		Runtime.getRuntime().addShutdownHook(new Thread(manager::dropTables));

		var tracked = new TrackingDataSource(manager.getDataSource());
		var session = Utils.createSqlSession(tracked);

		var mapper = session.getMapper(CategoryMapper.class);
		Category.mapper = mapper;
		var controller = new Controller(new Repository(mapper));
		var api = new HttpAdapter(tracked, session, controller);

		var server = HttpServer.create(new InetSocketAddress(HOST_NAME, PORT), 0);
		server.createContext("/api/", wrapHandler(api));

		var wwwRoot = Path.of("web").toAbsolutePath();
		server.createContext("/", SimpleFileServer.createFileHandler(wwwRoot));

		server.start();
		System.out.println("Demo hosted on http://" + HOST_NAME + ":" + PORT);
	}
}
