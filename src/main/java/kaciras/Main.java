package kaciras;

import com.sun.net.httpserver.HttpServer;
import org.apache.ibatis.session.SqlSession;

import java.net.InetSocketAddress;

public final class Main {

	private static final String HOST_NAME = "localhost";
	private static final int PORT = 6666;

	private static SqlSession session;

	public static void main(String[] args) throws Exception {
		Utils.disableIllegalAccessWarning();

		session = Utils.createSqlSession();
		Utils.executeScript(session.getConnection(), "schema.sql");
		var mapper = session.getMapper(CategoryMapper.class);

		// 如果使用Spring，可以用@Configurable来注入此依赖。
		Category.categoryMapper = mapper;
		var server = HttpServer.create(new InetSocketAddress(HOST_NAME, PORT), 0);
		server.createContext("/", new HttpAPI(session.getConfiguration(), mapper));
		server.start();

		// 玩完记得把表删了
		Runnable cleanup = () -> Utils.dropTables(session.getConnection());
		Runtime.getRuntime().addShutdownHook(new Thread(cleanup));

		System.out.println("Demo hosted on http://" + HOST_NAME + ":" + PORT);
	}
}
