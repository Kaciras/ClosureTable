package kaciras;

import org.apache.ibatis.jdbc.ScriptRunner;

import java.sql.Connection;

public interface SQLOperation {

	void run(ScriptRunner runner, Connection connection) throws Exception;
}
