package kaciras;

import com.sun.net.httpserver.HttpExchange;

public interface UncheckedHttpHandler {

	void handle(HttpExchange exchange) throws Exception;
}
