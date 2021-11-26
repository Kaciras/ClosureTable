package kaciras;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DatabaseTestLifecycle.class)
final class TrackingDataSourceTest {

	public DataSource dataSource;

	private TrackingDataSource tracked;
	private Connection connection;

	@BeforeEach
	void setUp() throws SQLException {
		tracked = new TrackingDataSource(dataSource);
		connection = tracked.getConnection();
	}

	@AfterEach
	void cleanUp() throws SQLException {
		connection.close();
	}

	@Test
	void getExecutedSqls() throws Exception {
		var stat = connection.prepareStatement("UPDATE category SET name=? WHERE id=?");
		stat.setString(1, "new");
		stat.setInt(2, 7);
		stat.executeUpdate();

		var sqls = tracked.getExecutedSqls();
		assertThat(sqls).hasSize(1);
		assertThat(sqls[0]).isEqualTo("UPDATE category SET name='new' WHERE id=7");
	}

	@Test
	void reset() throws Exception {
		var stat = connection.prepareStatement("SELECT name FROM category WHERE id=?");
		stat.setInt(1, 7);
		stat.executeQuery();

		tracked.reset();

		assertThat(tracked.getExecutedSqls()).isEmpty();

		stat.setInt(1, 7);
		stat.executeQuery();
		assertThat(tracked.getExecutedSqls()).hasSize(1);
	}
}
