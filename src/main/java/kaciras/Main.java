package kaciras;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.SimpleFileServer;

import java.net.InetSocketAddress;
import java.nio.file.Path;

public final class Main {

	private static final String HOST_NAME = "localhost";
	private static final int PORT = 7777;

	/**
	 * 包装一个 HttpHandler，增加异常处理和自动关闭 HttpExchange 功能，
	 * 同时允许参数抛出更宽泛的异常。
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

	public static void main(String... args) throws Exception {
		// 连接数据库，并导入演示数据。
		var manager = DBManager.open();
		manager.importData();

		// 创建 Mybatis 的 SqlSession
		var tracked = new TrackingDataSource(manager.getDataSource());
		var session = Utils.createSqlSession(tracked);

		// 玩完记得把表删了。
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			session.close();
			manager.dropTables();
		}));

		// 获取 SqlMapper，创建仓库和控制器对象。
		var mapper = session.getMapper(CategoryMapper.class);
		Category.mapper = mapper;
		var controller = new Controller(tracked, new Repository(mapper));

		// 创建 HTTP 服务器，并注册 API 请求处理器。
		var server = HttpServer.create(new InetSocketAddress(HOST_NAME, PORT), 0);
		var api = new Dispatcher(tracked, session, controller);
		server.createContext("/api/", wrapHandler(api));

		// 让 HTTP 服务器处理 web 目录下的静态文件。
		var wwwRoot = Path.of("web").toAbsolutePath();
		server.createContext("/", SimpleFileServer.createFileHandler(wwwRoot));

		server.start();
		System.out.println("Demo hosted on http://" + HOST_NAME + ":" + PORT);
	}
}
