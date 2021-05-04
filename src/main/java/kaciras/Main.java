package kaciras;

import com.sun.net.httpserver.HttpServer;
import org.apache.ibatis.session.SqlSession;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public final class Main {

	private static SqlSession session;

	public static void main(String[] args) throws Exception {
		Utils.disableIllegalAccessWarning();

		session = Utils.createSqlSession();
		Utils.executeScript(session.getConnection(), "schema.sql");
		var mapper = session.getMapper(CategoryMapper.class);

		// 如果使用Spring，可以用@Configurable来注入此依赖。
		Category.categoryMapper = mapper;

		var server = HttpServer.create();
		server.createContext("/", new HttpAPI(session.getConfiguration(), mapper));
		server.bind(new InetSocketAddress(InetAddress.getLocalHost(), 6666), 0);
		server.start();

		// 玩完记得把表删了
		Runnable cleanup = () -> Utils.dropTables(session.getConnection());
		Runtime.getRuntime().addShutdownHook(new Thread(cleanup));
	}
}
