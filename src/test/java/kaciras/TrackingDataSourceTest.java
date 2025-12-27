package kaciras;

import kaciras.setup.DBManager;
import lombok.Cleanup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DatabaseTestLifecycle.class)
final class TrackingDataSourceTest {

	public DBManager manager;

	private TrackingDataSource dataSource;
	private Connection tracked;

	@BeforeEach
	void setUp() {
		dataSource = new TrackingDataSource(manager.getConnection());
		tracked = dataSource.getConnection();
	}

	@Test
	void getExecutedSql() throws Exception {
		@Cleanup var stat = tracked.prepareStatement("UPDATE category SET name=? WHERE id=?");
		stat.setString(1, "new");
		stat.setInt(2, 7);
		stat.executeUpdate();

		var sqls = dataSource.getExecutedSql();
		assertThat(sqls).hasSize(1);
		assertThat(sqls[0]).isEqualTo("UPDATE category SET name='new' WHERE id=7");
	}

	@Test
	void reset() throws Exception {
		@Cleanup var stat = tracked.prepareStatement("SELECT name FROM category WHERE id=?");
		stat.setInt(1, 7);
		stat.executeQuery();

		dataSource.reset();

		assertThat(dataSource.getExecutedSql()).isEmpty();

		stat.setInt(1, 7);
		stat.executeQuery();
		assertThat(dataSource.getExecutedSql()).hasSize(1);
	}
}
